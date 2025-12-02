import apiClient from '../client';
import  type { PayrollResponse } from '@/types';

/**
 * Payroll Employee Service
 * Handles employee payroll viewing
 */
class PayrollEmployeeService {
  /**
   * Get own payroll history
   */
  async getPayrollHistory(): Promise<PayrollResponse[]> {
    const response = await apiClient.get<PayrollResponse[]>('/employee/payroll/history');
    return response.data;
  }

  /**
   * Get specific payroll by ID
   */
  async getPayrollById(payrollId: number): Promise<PayrollResponse> {
    const response = await apiClient.get<PayrollResponse>(`/employee/payroll/${payrollId}`);
    return response.data;
  }
}

export default new PayrollEmployeeService();