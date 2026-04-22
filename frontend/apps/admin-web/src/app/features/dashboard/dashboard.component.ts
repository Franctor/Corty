import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { AuthService } from '@frontend/shared-auth';
import { DashboardStats } from '@frontend/shared-core';
import { DashboardService } from './dashboard.service';
import { DecimalPipe } from '@angular/common';
import { ChartConfiguration, ChartData } from 'chart.js';
import { BaseChartDirective } from 'ng2-charts';

@Component({
  selector: 'app-dashboard',
  templateUrl: 'dashboard.component.html',
  styleUrl: 'dashboard.component.scss',
  standalone: true,
  imports: [BaseChartDirective, DecimalPipe],
})
export class DashboardComponent implements OnInit {
  private authService  = inject(AuthService);
  private dashService  = inject(DashboardService);

  readonly role    = computed(() => this.authService.getRole());
  readonly isAdmin = computed(() => this.role() === 'ADMIN' || this.role() === 'SUPERADMIN');
  readonly greeting = computed(() =>
    this.isAdmin() ? 'Panel de administración' : 'Panel de tu organización'
  );

  readonly stats   = signal<DashboardStats | null>(null);
  readonly loading = signal(true);

  // ── Chart: Reservas últimos 30 días ────────────────────────────────────────
  readonly lineData    = signal<ChartData<'line'>>({ labels: [], datasets: [] });
  readonly lineOptions: ChartConfiguration<'line'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { display: false } },
    scales: {
      x: { ticks: { maxTicksLimit: 7, color: 'var(--color-text-muted)' }, grid: { display: false } },
      y: { beginAtZero: true, ticks: { color: 'var(--color-text-muted)' }, grid: { color: 'rgba(0,0,0,0.05)' } },
    },
  };

  // ── Chart: Ingresos últimas 8 semanas ──────────────────────────────────────
  readonly barData    = signal<ChartData<'bar'>>({ labels: [], datasets: [] });
  readonly barOptions: ChartConfiguration<'bar'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { display: false } },
    scales: {
      x: { ticks: { color: 'var(--color-text-muted)' }, grid: { display: false } },
      y: { beginAtZero: true, ticks: { color: 'var(--color-text-muted)' }, grid: { color: 'rgba(0,0,0,0.05)' } },
    },
  };

  // ── Chart: Por estado (donut) ──────────────────────────────────────────────
  readonly donutStatusData = signal<ChartData<'doughnut'>>({ labels: [], datasets: [] });
  readonly donutOptions: ChartConfiguration<'doughnut'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { position: 'right' } },
    cutout: '65%',
  };

  // ── Chart: Por deporte (donut) — admin only ────────────────────────────────
  readonly donutSportData = signal<ChartData<'doughnut'>>({ labels: [], datasets: [] });

  // ── Chart: Top pistas (barras horizontales) ────────────────────────────────
  readonly topCourtsData    = signal<ChartData<'bar'>>({ labels: [], datasets: [] });
  readonly topCourtsOptions: ChartConfiguration<'bar'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    indexAxis: 'y',
    plugins: { legend: { display: false } },
    scales: {
      x: { beginAtZero: true, ticks: { color: 'var(--color-text-muted)' }, grid: { color: 'rgba(0,0,0,0.05)' } },
      y: { ticks: { color: 'var(--color-text-muted)' }, grid: { display: false } },
    },
  };

  // ── Chart: Clubs por organización — admin only ─────────────────────────────
  readonly clubsOrgData    = signal<ChartData<'bar'>>({ labels: [], datasets: [] });
  readonly clubsOrgOptions: ChartConfiguration<'bar'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { display: false } },
    scales: {
      x: { ticks: { color: 'var(--color-text-muted)' }, grid: { display: false } },
      y: { beginAtZero: true, ticks: { color: 'var(--color-text-muted)' }, grid: { color: 'rgba(0,0,0,0.05)' } },
    },
  };

  private readonly STATUS_COLORS: Record<string, string> = {
    CONFIRMED:  '#58CC02',
    COMPLETED:  '#1CB0F6',
    PENDING:    '#FFC800',
    CANCELLED:  '#FF4B4B',
  };

  private readonly SPORT_PALETTE = ['#58CC02','#1CB0F6','#FF9600','#FF4B4B','#CE82FF','#00CD9C'];

  ngOnInit(): void {
    this.dashService.getStats().subscribe({
      next: (data) => {
        this.stats.set(data);
        this.loading.set(false);
        this.buildCharts(data);
      },
      error: () => this.loading.set(false),
    });
  }

  private buildCharts(data: DashboardStats): void {
    // Línea: reservas 30 días
    this.lineData.set({
      labels: data.bookingsLast30Days.map((d) => this.shortDate(d.date)),
      datasets: [{
        data: data.bookingsLast30Days.map((d) => d.count),
        borderColor: '#58CC02',
        backgroundColor: 'rgba(88,204,2,0.12)',
        tension: 0.4,
        fill: true,
        pointRadius: 3,
        pointBackgroundColor: '#58CC02',
      }],
    });

    // Barras: ingresos 8 semanas
    this.barData.set({
      labels: data.revenueLast8Weeks.map((w) => this.shortWeek(w.week)),
      datasets: [{
        data: data.revenueLast8Weeks.map((w) => w.revenue),
        backgroundColor: '#1CB0F6',
        borderRadius: 6,
      }],
    });

    // Donut: estados
    this.donutStatusData.set({
      labels: data.bookingsByStatus.map((s) => this.statusLabel(s.label)),
      datasets: [{
        data: data.bookingsByStatus.map((s) => s.count),
        backgroundColor: data.bookingsByStatus.map((s) => this.STATUS_COLORS[s.label] ?? '#aaa'),
        borderWidth: 0,
      }],
    });

    // Donut: deportes (admin)
    if (data.bookingsBySport?.length) {
      this.donutSportData.set({
        labels: data.bookingsBySport.map((s) => s.label),
        datasets: [{
          data: data.bookingsBySport.map((s) => s.count),
          backgroundColor: this.SPORT_PALETTE,
          borderWidth: 0,
        }],
      });
    }

    // Barras horizontales: top pistas
    this.topCourtsData.set({
      labels: data.topCourts.map((c) => c.label),
      datasets: [{
        data: data.topCourts.map((c) => c.count),
        backgroundColor: '#58CC02',
        borderRadius: 4,
      }],
    });

    // Barras: clubs por org (admin)
    if (data.clubsByOrganization?.length) {
      this.clubsOrgData.set({
        labels: data.clubsByOrganization.map((o) => o.label),
        datasets: [{
          data: data.clubsByOrganization.map((o) => o.count),
          backgroundColor: '#CE82FF',
          borderRadius: 6,
        }],
      });
    }
  }

  private shortDate(iso: string): string {
    const [, m, d] = iso.split('-');
    return `${d}/${m}`;
  }

  private shortWeek(yw: string): string {
    const s = yw.toString();
    return `S${s.slice(4)}`;
  }

  statusLabel(key: string): string {
    const map: Record<string, string> = {
      CONFIRMED: 'Confirmada', COMPLETED: 'Completada',
      PENDING: 'Pendiente',   CANCELLED: 'Cancelada',
    };
    return map[key] ?? key;
  }

  statusClass(status: string): string {
    const map: Record<string, string> = {
      CONFIRMED: 'badge--green', COMPLETED: 'badge--blue',
      PENDING: 'badge--yellow',  CANCELLED: 'badge--red',
    };
    return map[status] ?? '';
  }
}
