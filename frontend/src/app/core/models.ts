export type Role = 'STUDENT' | 'COMPANY' | 'ADMIN';
export type ApplicationStatus = 'PENDING' | 'ACCEPTED' | 'REJECTED';

export interface User {
  id: number;
  fullName: string;
  email: string;
  role: Role;
  organization?: string;
}

export interface AuthResponse {
  token: string;
  expiresIn: number;
  user: User;
}

export interface Offer {
  id: number;
  title: string;
  description: string;
  location: string;
  durationMonths: number;
  skills?: string;
  open: boolean;
  companyId: number;
  companyName: string;
  createdAt: string;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
}

export interface Application {
  id: number;
  offerId: number;
  offerTitle: string;
  companyName: string;
  studentName: string;
  studentEmail: string;
  studentSchool?: string;
  motivation: string;
  status: ApplicationStatus;
  createdAt: string;
}

export interface Stats {
  openOffers: number;
  applications: number;
  pending: number;
  accepted: number;
  rejected: number;
  students: number;
}
