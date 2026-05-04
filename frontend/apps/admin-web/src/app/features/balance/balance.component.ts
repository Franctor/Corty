import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { DecimalPipe, DatePipe } from '@angular/common';
import { ChartConfiguration, ChartData } from 'chart.js';
import { BaseChartDirective } from 'ng2-charts';
import { AuthService } from '@frontend/shared-auth';
import { ClubResponse, ClubStatsResponse, OrgAdminResponse } from '@frontend/shared-core';
import { ClubAdminService } from '../../core/services/club-admin.service';
import { OrgAdminService } from '../../core/services/org-admin.service';
import { ToastService } from '../../shared/services/toast.service';
import { AdminPageHeaderComponent } from '../../shared/components/admin-page-header/admin-page-header.component';
import { SelectComponent, SelectOption } from '@frontend/shared-ui';

const REASON_LABELS: Record<string, string> = {
  PARTICIPANT_LATE_CANCEL:        'Jugador: cancelación tardía (2–24h)',
  PARTICIPANT_LAST_MINUTE_CANCEL: 'Jugador: cancelación de última hora (<2h)',
  OWNER_LATE_CANCEL:              'Owner: cancelación tardía (2–24h)',
  OWNER_LAST_MINUTE_CANCEL:       'Owner: cancelación de última hora (<2h)',
};

const REASON_COLORS = ['#FF4B4B', '#FF9600', '#CE82FF', '#1CB0F6'];

@Component({
  selector: 'app-balance',
  templateUrl: 'balance.component.html',
  styleUrl: 'balance.component.scss',
  standalone: true,
  imports: [AdminPageHeaderComponent, SelectComponent, BaseChartDirective, DecimalPipe, DatePipe],
})
export class BalanceComponent implements OnInit {
  private auth        = inject(AuthService);
  private clubService = inject(ClubAdminService);
  private orgService  = inject(OrgAdminService);
  private toast       = inject(ToastService);

  readonly isSuperAdmin = computed(() => this.auth.getRole() === 'SUPERADMIN' || this.auth.getRole() === 'ADMIN');
  readonly isOrg        = computed(() => this.auth.getRole() === 'ORGANIZATION');

  readonly orgs          = signal<OrgAdminResponse[]>([]);
  readonly orgOptions    = signal<SelectOption<number>[]>([]);
  readonly selectedOrgId = signal<number | null>(null);

  readonly clubs          = signal<ClubResponse[]>([]);
  readonly clubOptions    = signal<SelectOption<number>[]>([]);
  readonly selectedClubId = signal<number | null>(null);

  readonly stats   = signal<ClubStatsResponse | null>(null);
  readonly loading = signal(false);

  // ── Gráfica 1: Ingresos por mes (barras) ──────────────────────────────────
  readonly revenueBarData = signal<ChartData<'bar'>>({ labels: [], datasets: [] });
  readonly revenueBarOptions: ChartConfiguration<'bar'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { display: true, position: 'top' } },
    scales: {
      x: { ticks: { color: 'var(--color-text-muted)' }, grid: { display: false } },
      y: { beginAtZero: true, ticks: { color: 'var(--color-text-muted)', callback: (v) => `${v} €` }, grid: { color: 'rgba(0,0,0,0.05)' } },
    },
  };

  // ── Gráfica 2: Donut por motivo de penalización ────────────────────────────
  readonly penaltyDonutData = signal<ChartData<'doughnut'>>({ labels: [], datasets: [] });
  readonly donutOptions: ChartConfiguration<'doughnut'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { position: 'right' } },
    cutout: '65%',
  };

  // ── Gráfica 3: Evolución acumulativa del balance (línea) ───────────────────
  readonly cumulativeLineData = signal<ChartData<'line'>>({ labels: [], datasets: [] });
  readonly lineOptions: ChartConfiguration<'line'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { display: true, position: 'top' } },
    scales: {
      x: { ticks: { color: 'var(--color-text-muted)', maxTicksLimit: 8 }, grid: { display: false } },
      y: { beginAtZero: true, ticks: { color: 'var(--color-text-muted)', callback: (v) => `${v} €` }, grid: { color: 'rgba(0,0,0,0.05)' } },
    },
  };

  ngOnInit(): void {
    if (this.isSuperAdmin()) {
      this.orgService.getAll().subscribe({
        next: (orgs) => {
          this.orgs.set(orgs);
          this.orgOptions.set(orgs.map(o => ({ value: o.id, label: o.businessName })));
          if (orgs.length > 0) this.onOrgChange(orgs[0].id);
        },
        error: () => this.toast.error('Error al cargar organizaciones'),
      });
    } else {
      // ORGANIZATION: solo sus clubes
      this.clubService.getAll().subscribe({
        next: (clubs) => this.setClubs(clubs),
        error: () => this.toast.error('Error al cargar clubes'),
      });
    }
  }

  onOrgChange(orgId: number | null): void {
    if (orgId == null) return;
    this.selectedOrgId.set(orgId);
    this.selectedClubId.set(null);
    this.stats.set(null);
    this.clubService.getByOrg(orgId).subscribe({
      next: (clubs) => this.setClubs(clubs),
      error: () => this.toast.error('Error al cargar clubes'),
    });
  }

  onClubChange(clubId: number | null): void {
    if (clubId == null) return;
    this.selectedClubId.set(clubId);
    this.loadStats(clubId);
  }

  private setClubs(clubs: ClubResponse[]): void {
    this.clubs.set(clubs);
    this.clubOptions.set(clubs.map(c => ({ value: c.id, label: c.name })));
    if (clubs.length > 0) {
      this.selectedClubId.set(clubs[0].id);
      this.loadStats(clubs[0].id);
    }
  }

  private loadStats(clubId: number): void {
    this.loading.set(true);
    this.clubService.getStats(clubId).subscribe({
      next: (data) => {
        this.stats.set(data);
        this.loading.set(false);
        this.buildCharts(data);
      },
      error: () => {
        this.loading.set(false);
        this.toast.error('Error al cargar estadísticas');
      },
    });
  }

  private buildCharts(data: ClubStatsResponse): void {
    const allMonths = this.mergeMonths(data.revenueByMonth, data.penaltiesByMonth);

    // Barras agrupadas: ingresos vs penalizaciones por mes
    this.revenueBarData.set({
      labels: allMonths,
      datasets: [
        {
          label: 'Ingresos netos (reservas)',
          data: allMonths.map(m => data.revenueByMonth[m] ?? 0),
          backgroundColor: '#1CB0F6',
          borderRadius: 4,
        },
        {
          label: 'Penalizaciones',
          data: allMonths.map(m => data.penaltiesByMonth[m] ?? 0),
          backgroundColor: '#FF4B4B',
          borderRadius: 4,
        },
      ],
    });

    // Donut: penalizaciones por motivo
    const reasons = Object.keys(data.penaltiesByReason);
    this.penaltyDonutData.set({
      labels: reasons.map(r => REASON_LABELS[r] ?? r),
      datasets: [{
        data: reasons.map(r => data.penaltiesByReason[r]),
        backgroundColor: REASON_COLORS,
        borderWidth: 0,
      }],
    });

    // Línea acumulativa: ingresos y penalizaciones acumulados mes a mes
    let cumRevenue = 0;
    let cumPenalty = 0;
    const cumRevenueData = allMonths.map(m => { cumRevenue += data.revenueByMonth[m] ?? 0; return +cumRevenue.toFixed(2); });
    const cumPenaltyData = allMonths.map(m => { cumPenalty += data.penaltiesByMonth[m] ?? 0; return +cumPenalty.toFixed(2); });

    this.cumulativeLineData.set({
      labels: allMonths,
      datasets: [
        {
          label: 'Ingresos acumulados',
          data: cumRevenueData,
          borderColor: '#1CB0F6',
          backgroundColor: 'rgba(28,176,246,0.1)',
          tension: 0.4,
          fill: true,
          pointRadius: 3,
          pointBackgroundColor: '#1CB0F6',
        },
        {
          label: 'Penalizaciones acumuladas',
          data: cumPenaltyData,
          borderColor: '#FF4B4B',
          backgroundColor: 'rgba(255,75,75,0.1)',
          tension: 0.4,
          fill: true,
          pointRadius: 3,
          pointBackgroundColor: '#FF4B4B',
        },
      ],
    });
  }

  reasonLabel(reason: string): string {
    return REASON_LABELS[reason] ?? reason;
  }

  private mergeMonths(...maps: Record<string, number>[]): string[] {
    const set = new Set<string>();
    for (const map of maps) Object.keys(map).forEach(k => set.add(k));
    return [...set].sort();
  }
}
