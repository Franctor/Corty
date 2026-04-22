import { Component, inject, input, output, signal, computed, effect } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { LucideAngularModule } from 'lucide-angular';
import { TableColumn } from '@frontend/shared-core';

const DEFAULT_PAGE_SIZE = 10;

@Component({
  selector: 'app-admin-table',
  templateUrl: 'admin-table.component.html',
  styleUrl: 'admin-table.component.scss',
  standalone: true,
  imports: [LucideAngularModule, FormsModule],
})
export class AdminTableComponent<T extends { id?: number | string }> {
  private sanitizer = inject(DomSanitizer);

  readonly columns   = input.required<TableColumn<T>[]>();
  readonly rows      = input.required<T[]>();
  readonly loading   = input(false);
  readonly canDelete = input<(row: T) => boolean>(() => true);
  readonly canEdit   = input<(row: T) => boolean>(() => true);
  readonly pageSize  = input(DEFAULT_PAGE_SIZE);

  // Server-side pagination — parent handles fetching and searching
  readonly serverTotal  = input<number | null>(null);
  readonly serverPage   = input<number>(0);
  readonly pageChange   = output<number>();
  readonly searchChange = output<string>();

  readonly editRow   = output<T>();
  readonly deleteRow = output<T>();

  readonly searchQuery = signal('');
  readonly currentPage = signal(0);

  constructor() {
    // Keep currentPage in sync when parent changes serverPage
    effect(() => { this.currentPage.set(this.serverPage()); }, { allowSignalWrites: true });
  }

  readonly isServerMode = computed(() => this.serverTotal() !== null);

  readonly filteredRows = computed(() => {
    if (this.isServerMode()) return this.rows();
    const query = this.searchQuery().toLowerCase().trim();
    if (!query) return this.rows();
    return this.rows().filter((row) =>
      this.columns().some((col) =>
        this.getCellValue(row, col).toString().toLowerCase().includes(query)
      )
    );
  });

  readonly totalPages = computed(() => {
    const size = this.pageSize();
    const total = this.isServerMode() ? this.serverTotal()! : this.filteredRows().length;
    return Math.max(1, Math.ceil(total / size));
  });

  readonly totalElements = computed(() =>
    this.isServerMode() ? this.serverTotal()! : this.filteredRows().length
  );

  readonly pagedRows = computed(() => {
    if (this.isServerMode()) return this.rows();
    const page = this.currentPage();
    const size = this.pageSize();
    return this.filteredRows().slice(page * size, page * size + size);
  });

  readonly pageNumbers = computed((): (number | '...')[] => {
    const total = this.totalPages();
    const current = this.currentPage();
    if (total <= 7) return Array.from({ length: total }, (_, index) => index);
    const pages: (number | '...')[] = [0];
    if (current > 2) pages.push('...');
    for (let pageIndex = Math.max(1, current - 1); pageIndex <= Math.min(total - 2, current + 1); pageIndex++) {
      pages.push(pageIndex);
    }
    if (current < total - 3) pages.push('...');
    pages.push(total - 1);
    return pages;
  });

  onSearch(query: string): void {
    this.searchQuery.set(query);
    this.currentPage.set(0);
    if (this.isServerMode()) this.searchChange.emit(query);
  }

  goToPage(page: number | '...'): void {
    if (page === '...') return;
    this.currentPage.set(page);
    if (this.isServerMode()) this.pageChange.emit(page);
  }

  prevPage(): void {
    const page = this.currentPage();
    if (page > 0) this.goToPage(page - 1);
  }

  nextPage(): void {
    const page = this.currentPage();
    if (page < this.totalPages() - 1) this.goToPage(page + 1);
  }

  getCellValue(row: T, col: TableColumn<T>): string {
    if (col.render) return col.render(row);
    const keys = (col.key as string).split('.');
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    let val: any = row;
    for (const key of keys) val = val?.[key];
    return val ?? '—';
  }

  getSafeHtml(row: T, col: TableColumn<T>): SafeHtml {
    return this.sanitizer.bypassSecurityTrustHtml(this.getCellValue(row, col));
  }
}
