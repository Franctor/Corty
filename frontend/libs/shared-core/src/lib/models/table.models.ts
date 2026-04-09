export interface TableColumn<T> {
  key: keyof T | string;
  label: string;
  render?: (row: T) => string;
}
