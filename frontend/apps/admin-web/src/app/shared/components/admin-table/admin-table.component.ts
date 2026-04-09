import { Component, input, output } from '@angular/core';
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
  readonly columns = input.required<TableColumn<T>[]>();
  readonly rows = input.required<T[]>();
  readonly loading = input(false);

  readonly editRow = output<T>();
  readonly deleteRow = output<T>();

  getCellValue(row: T, col: TableColumn<T>): string {
    if (col.render) return col.render(row);
    const keys = (col.key as string).split('.');
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    let val: any = row;
    for (const k of keys) val = val?.[k];
    return val ?? '—';
  }
}
