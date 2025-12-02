import payrollEmployeeServices from "@/api/services/payrollEmployeeServices";
import { useQuery } from "@tanstack/react-query";
import type {
  PayrollResponse,
  ErrorResponse,
} from '@/types';
import { AxiosError } from 'axios';

/**
 * Hook to get employee payroll history
 */
export const useEmployeePayrollHistory = () => {
  return useQuery<PayrollResponse[], AxiosError<ErrorResponse>>({
    queryKey: ['employeePayrollHistory'],
    queryFn: payrollEmployeeServices.getPayrollHistory,
    staleTime: 10 * 60 * 1000, // 10 minutes
  });
};

/**
 * Hook to get specific payroll by ID
 */
export const useEmployeePayrollById = (payrollId: number | undefined) => {
  return useQuery<PayrollResponse, AxiosError<ErrorResponse>>({
    queryKey: ['employeePayroll', payrollId],
    queryFn: () => payrollEmployeeServices.getPayrollById(payrollId!),
    enabled: !!payrollId,
    staleTime: 10 * 60 * 1000,
  });
};