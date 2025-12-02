import apiClient from '../client';
import type {
  DashboardResponse,
  EmployeeResponse,
  PayInfoResponse,
  ContactInfoResponse,
  UpdateContactInfoRequest,
  MessageResponse,
} from '@/types';

/**
 * Employee Service
 * Handles employee-related API calls (common endpoints for all roles)
 */
class EmployeeService {
  /**
   * Get employee dashboard
   */
  async getDashboard(): Promise<DashboardResponse> {
    const response = await apiClient.get<DashboardResponse>('/employee/dashboard');
    return response.data;
  }

  /**
   * Get personal information
   */
  async getPersonalInfo(): Promise<EmployeeResponse> {
    const response = await apiClient.get<EmployeeResponse>('/employee/personal-info');
    return response.data;
  }

  /**
   * Get pay information
   */
  async getPayInfo(): Promise<PayInfoResponse> {
    const response = await apiClient.get<PayInfoResponse>('/employee/pay-info');
    return response.data;
  }

  /**
   * Get contact information
   */
  async getContactInfo(): Promise<ContactInfoResponse> {
    const response = await apiClient.get<ContactInfoResponse>('/employee/contact-info');
    return response.data;
  }

  /**
   * Update contact information
   */
  async updateContactInfo(data: UpdateContactInfoRequest): Promise<MessageResponse> {
    const response = await apiClient.put<MessageResponse>('/employee/contact-info', data);
    return response.data;
  }

  /**
   * Get user profile
   */
  async getProfile(): Promise<EmployeeResponse> {
    const response = await apiClient.get<EmployeeResponse>('/employee/profile');
    return response.data;
  }
}

export default new EmployeeService();