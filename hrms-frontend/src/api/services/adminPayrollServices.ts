import apiClient from '../client';
import type {
  PayrollPreviewResponse,
  PayrollResponse,
  RunPayrollRequest,
  RunPayrollResponse,
  PayrollWeekStatus,
} from '@/types';

/**
 * Admin Payroll Service
 * Handles admin payroll operations
 */
class AdminPayrollService {
  /**
   * Preview payroll for a week
   */
  async previewPayroll(weekStartDate: string): Promise<PayrollPreviewResponse> {
    const response = await apiClient.get<PayrollPreviewResponse>('/admin/payroll/preview', {
      params: { weekStartDate },
    });
    return response.data;
  }

  /**
   * Run/process payroll
   */
  async runPayroll(data: RunPayrollRequest): Promise<RunPayrollResponse> {
    const response = await apiClient.post<RunPayrollResponse>('/admin/payroll/run', data);
    return response.data;
  }

  /**
   * Get payroll by ID
   */
  async getPayrollById(payrollId: number): Promise<PayrollResponse> {
    const response = await apiClient.get<PayrollResponse>(`/admin/payroll/${payrollId}`);
    return response.data;
  }

  /**
   * Get payroll history by date range
   */
  async getPayrollHistory(startDate: string, endDate: string): Promise<PayrollResponse[]> {
    const response = await apiClient.get<PayrollResponse[]>('/admin/payroll/history', {
      params: { startDate, endDate },
    });
    return response.data;
  }

  /**
   * Mark payroll as paid
   */
  async markPayrollAsPaid(payrollId: number): Promise<void> {
    await apiClient.patch(`/admin/payroll/${payrollId}/mark-paid`);
  }

  /**
   * Get current week payroll status
   */
  async getCurrentWeekStatus(): Promise<PayrollWeekStatus> {
    const response = await apiClient.get<PayrollWeekStatus>('/admin/payroll/current-week');
    return response.data;
  }
}

export default new AdminPayrollService();