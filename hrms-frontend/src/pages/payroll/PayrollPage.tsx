import { Outlet } from 'react-router-dom';
import { PageHeader } from '@/components/common';

export const PayrollPage = () => {
  return (
    <div className="space-y-6">
      <PageHeader
        title="My Payroll"
        description="View your payment history and payslips"
      />
      <Outlet />
    </div>
  );
};

export default PayrollPage;

