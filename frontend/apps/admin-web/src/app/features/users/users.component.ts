import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { DatePipe } from '@angular/common';
import { UserAdminService } from '../../core/services/user-admin.service';
import { UserAdminResponse, TableColumn } from '@frontend/shared-core';
import { AdminPageHeaderComponent } from '../../shared/components/admin-page-header/admin-page-header.component';
import { AdminTableComponent } from '../../shared/components/admin-table/admin-table.component';
import { AdminModalComponent } from '../../shared/components/admin-modal/admin-modal.component';
import { ConfirmModalComponent } from '../../shared/components/confirm-modal/confirm-modal.component';
import { ToastService } from '../../shared/services/toast.service';
import { AuthService } from '@frontend/shared-auth';
import { SelectComponent, SelectOption } from '@frontend/shared-ui';

@Component({
  selector: 'app-users',
  templateUrl: 'users.component.html',
  styleUrl: 'users.component.scss',
  standalone: true,
  imports: [
    AdminPageHeaderComponent,
    AdminTableComponent,
    AdminModalComponent,
    ConfirmModalComponent,
    DatePipe,
    SelectComponent,
  ],
})
export class UsersComponent implements OnInit {
  private service = inject(UserAdminService);
  private toast = inject(ToastService);
  private auth = inject(AuthService);

  readonly canManageRoles = computed(() => this.auth.hasAuthority('MANAGE_ROLES'));

  readonly users = signal<UserAdminResponse[]>([]);
  readonly totalUsers = signal<number | null>(null);
  readonly currentPage = signal(0);
  readonly searchQuery = signal('');
  readonly sortKey = signal('username');
  readonly sortDir = signal<'asc' | 'desc'>('asc');
  readonly availableAuthorities = signal<string[]>([]);
  readonly loading = signal(false);
  readonly savingRole = signal(false);
  readonly deletingItem = signal<UserAdminResponse | null>(null);
  readonly statusItem = signal<UserAdminResponse | null>(null);
  readonly statusReason = signal('');

  readonly roleOptions = signal<SelectOption<string>[]>([]);
  readonly selectedRole = signal<string | null>(null);
  readonly selectedAuthorities = signal<string[]>([]);

  readonly canDeleteUser = (user: UserAdminResponse) => user.role !== 'SUPERADMIN';
  readonly canEditUser = (user: UserAdminResponse) => user.role !== 'SUPERADMIN';

  readonly columns: TableColumn<UserAdminResponse>[] = [
    { key: 'username', label: 'Usuario', sortable: true },
    { key: 'email', label: 'Email', sortable: true },
    { key: 'role', label: 'Rol', sortable: true },
    { key: 'enabled', label: 'Activo', render: (r) => r.enabled ? 'Sí' : 'No' },
    { key: 'locked', label: 'Bloqueado', render: (r) => r.locked ? 'Sí' : 'No' },
    { key: 'creationDate', label: 'Registro', sortable: true, render: (r) => new Date(r.creationDate).toLocaleDateString('es-ES') },
  ];

  ngOnInit(): void {
    this.loadUsers();
    if (this.canManageRoles()) {
      this.service.getRoles().subscribe(roles =>
        this.roleOptions.set(roles.map(r => ({ value: r, label: r })))
      );
      this.service.getAuthorities().subscribe(a => this.availableAuthorities.set(a));
    }
  }

  private loadUsers(page = 0, search = ''): void {
    this.loading.set(true);
    this.service.getAll(page, 10, search, this.sortKey(), this.sortDir()).subscribe({
      next: (data) => { this.users.set(data.content); this.totalUsers.set(data.page.totalElements); this.loading.set(false); },
      error: () => { this.loading.set(false); this.toast.error('Error al cargar los usuarios'); },
    });
  }

  onPageChange(page: number): void {
    this.currentPage.set(page);
    this.loadUsers(page, this.searchQuery());
  }

  onSearch(query: string): void {
    this.searchQuery.set(query);
    this.currentPage.set(0);
    this.loadUsers(0, query);
  }

  onSortChange(ev: { key: string; dir: 'asc' | 'desc' }): void {
    this.sortKey.set(ev.key);
    this.sortDir.set(ev.dir);
    this.currentPage.set(0);
    this.loadUsers(0, this.searchQuery());
  }

  openEdit(user: UserAdminResponse): void {
    this.statusItem.set(user);
    this.selectedRole.set(user.role);
    this.selectedAuthorities.set([...user.authorities]);
  }

  toggleEnabled(user: UserAdminResponse): void {
    const reason = this.statusReason().trim() || undefined;
    this.statusReason.set('');
    this.service.updateStatus(user.id, { enabled: !user.enabled, locked: user.locked, reason }).subscribe({
      next: (updated) => {
        this.users.update(list => list.map(u => u.id === updated.id ? updated : u));
        this.statusItem.set(updated);
        this.toast.success(`Usuario ${updated.enabled ? 'activado' : 'desactivado'}`);
      },
      error: () => this.toast.error('Error al actualizar el estado'),
    });
  }

  toggleLocked(user: UserAdminResponse): void {
    const reason = this.statusReason().trim() || undefined;
    this.statusReason.set('');
    this.service.updateStatus(user.id, { enabled: user.enabled, locked: !user.locked, reason }).subscribe({
      next: (updated) => {
        this.users.update(list => list.map(u => u.id === updated.id ? updated : u));
        this.statusItem.set(updated);
        this.toast.success(`Usuario ${updated.locked ? 'bloqueado' : 'desbloqueado'}`);
      },
      error: () => this.toast.error('Error al actualizar el estado'),
    });
  }

  toggleAuthority(name: string): void {
    this.selectedAuthorities.update(list =>
      list.includes(name) ? list.filter(a => a !== name) : [...list, name]
    );
  }

  hasAuthority(name: string): boolean {
    return this.selectedAuthorities().includes(name);
  }

  saveRoleAndAuthorities(): void {
    const user = this.statusItem();
    if (!user || this.savingRole()) return;
    this.savingRole.set(true);
    this.service.updateRoleAndAuthorities(user.id, {
      role: this.selectedRole() ?? user.role,
      authorities: this.selectedAuthorities(),
    }).subscribe({
      next: (updated) => {
        this.users.update(list => list.map(u => u.id === updated.id ? updated : u));
        this.statusItem.set(updated);
        this.savingRole.set(false);
        this.toast.success('Rol y permisos actualizados');
      },
      error: () => {
        this.savingRole.set(false);
        this.toast.error('Error al actualizar el rol');
      },
    });
  }

  onDelete(user: UserAdminResponse): void {
    this.deletingItem.set(user);
  }

  confirmDelete(): void {
    const user = this.deletingItem();
    if (!user) return;
    this.deletingItem.set(null);
    this.service.delete(user.id).subscribe({
      next: () => {
        this.users.update(list => list.filter(u => u.id !== user.id));
        this.toast.success(`"${user.username}" eliminado`);
      },
      error: (err: HttpErrorResponse) =>
        this.toast.error(err.error?.message ?? 'Error al eliminar el usuario'),
    });
  }
}
