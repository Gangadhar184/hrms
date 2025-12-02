import apiClient from '../client';
import type {
  TimesheetResponse,
  UpdateTimesheetRequest,
  MessageResponse,
} from '@/types';

/**
 * Timesheet Service
 * Handles employee timesheet operations
 */
class TimesheetService {
  /**
   * Get current week timesheet
   */
  async getCurrentTimesheet(): Promise<TimesheetResponse> {
    const response = await apiClient.get<TimesheetResponse>('/employee/timesheet/current');
    return response.data;
  }

  /**
   * Get timesheet by ID
   */
  async getTimesheetById(timesheetId: number): Promise<TimesheetResponse> {
    const response = await apiClient.get<TimesheetResponse>(
      `/employee/timesheet/${timesheetId}`
    );
    return response.data;
  }

  /**
   * Get timesheet history
   */
  async getTimesheetHistory(): Promise<TimesheetResponse[]> {
    const response = await apiClient.get<TimesheetResponse[]>('/employee/timesheet/history');
    return response.data;
  }

  /**
   * Update timesheet entries
   */
  async updateTimesheet(
    timesheetId: number,
    data: UpdateTimesheetRequest
  ): Promise<TimesheetResponse> {
    const response = await apiClient.put<TimesheetResponse>(
      `/employee/timesheet/${timesheetId}`,
      data
    );
    return response.data;
  }

  /**
   * Submit timesheet for approval
   */
  async submitTimesheet(timesheetId: number): Promise<MessageResponse> {
    const response = await apiClient.post<MessageResponse>(
      `/employee/timesheet/${timesheetId}/submit`
    );
    return response.data;
  }
}

export default new TimesheetService();