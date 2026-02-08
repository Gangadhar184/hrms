import apiClient from '../client';
import type {
  EmployeeResponse,
  TimesheetListResponse,
  TimesheetResponse,
  PageResponse,
  TeamTimesheetParams,
  MessageResponse,
  ManagerStatistics,
} from '@/types';

/**
 * Manager Service
 * Handles manager-specific operations
 */
class ManagerService {
  /**
   * Get direct reports (employees reporting to manager)
   */
  async getDirectReports(): Promise<EmployeeResponse[]> {
    const response = await apiClient.get<EmployeeResponse[]>('/manager/employees');
    return response.data;
  }

  /**
   * Get team timesheets with pagination
   */
  async getTeamTimesheets(
    params: TeamTimesheetParams = {}
  ): Promise<PageResponse<TimesheetListResponse>> {
    const queryParams = new URLSearchParams({
      status: params.status || 'SUBMITTED',
      page: String(params.page || 0),
      size: String(params.size || 20),
      sort: params.sort || 'submittedAt',
      direction: params.direction || 'asc',
    });

    const response = await apiClient.get<PageResponse<TimesheetListResponse>>(
      `/manager/timesheets?${queryParams}`
    );
    return response.data;
  }

  /**
   * Get timesheet details by ID (for direct reports)
   */
  async getTimesheetById(timesheetId: number): Promise<TimesheetResponse> {
    const response = await apiClient.get<TimesheetResponse>(
      `/manager/timesheets/${timesheetId}`
    );
    return response.data;
  }

  /**
   * Get pending timesheets count
   */
  async getPendingTimesheetsCount(): Promise<number> {
    const response = await apiClient.get<number>('/manager/timesheets/pending/count');
    return response.data;
  }

  /**
   * Approve timesheet
   */
  async approveTimesheet(timesheetId: number): Promise<MessageResponse> {
    const response = await apiClient.post<MessageResponse>(
      `/manager/timesheets/${timesheetId}/approve`
    );
    return response.data;
  }

  /**
   * Deny timesheet with reason
   */
  async denyTimesheet(timesheetId: number, reason: string): Promise<MessageResponse> {
    const response = await apiClient.post<MessageResponse>(
      `/manager/timesheets/${timesheetId}/deny`,
      { reason }
    );
    return response.data;
  }

  /**
   * Get manager statistics
   */
  async getStatistics(): Promise<ManagerStatistics> {
    const response = await apiClient.get<ManagerStatistics>('/manager/statistics');
    return response.data;
  }
}

export default new ManagerService();