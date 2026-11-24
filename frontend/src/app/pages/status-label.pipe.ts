import { Pipe, PipeTransform } from '@angular/core';
import { ApplicationStatus } from '../core/models';

const LABELS: Record<ApplicationStatus, string> = {
  PENDING: 'En attente',
  ACCEPTED: 'Acceptée',
  REJECTED: 'Refusée',
};

@Pipe({ name: 'statusLabel', standalone: true })
export class StatusLabelPipe implements PipeTransform {
  transform(status: ApplicationStatus): string {
    return LABELS[status] ?? status;
  }
}
