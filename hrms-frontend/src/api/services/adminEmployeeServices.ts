import apiClient from '../client';
import type {
  EmployeeResponse,
  EmployeeListResponse,
  PageResponse,
  CreateEmployeeRequest,
  CreateEmployeeResponse,
  UpdateEmployeePersonalInfoRequest,
  UpdatePayInfoRequest,
  EmployeeSearchParams,
  EmployeeStatistics,
  MessageResponse,
} from '@/types';

/**
 * Admin Employee Service
 * Handles admin employee management operations
 */
class AdminEmployeeService {
  /**
   * Get all employees with pagination and filters
   */
  async getAllEmployees(
    params: EmployeeSearchParams = {}
  ): Promise<PageResponse<EmployeeListResponse>> {
    const queryParams = new URLSearchParams();
    
    if (params.search) queryParams.append('search', params.search);
    if (params.role) queryParams.append('role', params.role);
    queryParams.append('page', String(params.page || 0));
    queryParams.append('size', String(params.size || 20));
    queryParams.append('sort', params.sort || 'lastName');
    queryParams.append('direction', params.direction || 'asc');

    const response = await apiClient.get<PageResponse<EmployeeListResponse>>(
      `/admin/employees?${queryParams}`
    );
    return response.data;
  }

  /**
   * Get employee by ID
   */
  async getEmployeeById(employeeId: number): Promise<EmployeeResponse> {
    const response = await apiClient.get<EmployeeResponse>(`/admin/employees/${employeeId}`);
    return response.data;
  }

  /**
   * Create new employee
   */
  async createEmployee(data: CreateEmployeeRequest): Promise<CreateEmployeeResponse> {
    const response = await apiClient.post<CreateEmployeeResponse>('/admin/employees', data);
    return response.data;
  }

  /**
   * Update employee personal information
   */
  async updatePersonalInfo(
    employeeId: number,
    data: UpdateEmployeePersonalInfoRequest
  ): Promise<MessageResponse> {
    const response = await apiClient.put<MessageResponse>(
      `/admin/employees/${employeeId}/personal-info`,
      data
    );
    return response.data;
  }

  /**
   * Update employee pay information
   */
  async updatePayInfo(
    employeeId: number,
    data: UpdatePayInfoRequest
  ): Promise<MessageResponse> {
    const response = await apiClient.put<MessageResponse>(
      `/admin/employees/${employeeId}/pay-info`,
      data
    );
    return response.data;
  }

  /**
   * Get all active managers (for dropdown)
   */
  async getActiveManagers(): Promise<EmployeeResponse[]> {
    const response = await apiClient.get<EmployeeResponse[]>('/admin/employees/managers');
    return response.data;
  }

  /**
   * Get employee statistics
   */
  async getStatistics(): Promise<EmployeeStatistics> {
    const response = await apiClient.get<EmployeeStatistics>(
      '/admin/employees/statistics/count-by-role'
    );
    return response.data;
  }
}

export default new AdminEmployeeService();