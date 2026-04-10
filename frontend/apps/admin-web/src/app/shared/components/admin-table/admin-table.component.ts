import { Component, inject, input, output } from '@angular/core';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { LucideAngularModule } from 'lucide-angular';
import { TableColumn } from '@frontend/shared-core';

@Component({
  selector: 'app-admin-table',
  templateUrl: 'admin-table.component.html',
  styleUrl: 'admin-table.component.scss',
  standalone: true,
  imports: [LucideAngularModule],
})
export class AdminTableComponent<T extends { id?: number | string }> {
  private sanitizer = inject(DomSanitizer);

  readonly columns = input.required<TableColumn<T>[]>();
  readonly rows = input.required<T[]>();
  readonly loading = input(false);

  readonly editRow = output<T>();
  readonly deleteRow = output<T>();
  /** Función predicado opcional — si devuelve false, el botón de borrar se oculta para esa fila */
  readonly canDelete = input<(row: T) => boolean>(() => true);
  readonly canEdit = input<(row: T) => boolean>(() => true);

  getCellValue(row: T, col: TableColumn<T>): string {
    if (col.render) return col.render(row);
    const keys = (col.key as string).split('.');
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    let val: any = row;
    for (const k of keys) val = val?.[k];
    return val ?? '—';
  }

  getSafeHtml(row: T, col: TableColumn<T>): SafeHtml {
    return this.sanitizer.bypassSecurityTrustHtml(this.getCellValue(row, col));
  }
}
