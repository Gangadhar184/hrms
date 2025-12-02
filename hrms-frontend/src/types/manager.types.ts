/**
 * Manager Types
 */

import { TimesheetStatus } from './common.types';

// Team Timesheet Params
export interface TeamTimesheetParams {
  status?: TimesheetStatus;
  page?: number;
  size?: number;
  sort?: string;
  direction?: 'asc' | 'desc';
}

// Manager Statistics
export interface ManagerStatistics {
  directReportsCount: number;
  pendingTimesheetsCount: number;
}