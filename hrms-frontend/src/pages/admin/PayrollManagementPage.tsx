import { Link, Outlet, useLocation } from 'react-router-dom';
import { PlayCircle, History } from 'lucide-react';
import { cn } from '@/lib/utils';
import { PageHeader } from '@/components/common';

const tabs = [
  { name: 'Run Payroll', href: '/admin/payroll', icon: PlayCircle },
  { name: 'Payroll History', href: '/admin/payroll/history', icon: History },
];

export const PayrollManagementPage = () => {
  const location = useLocation();

  return (
    <div className="space-y-6">
      <PageHeader
        title="Payroll Management"
        description="Process payroll and view payment history"
      />

      <div className="border-b">
        <nav className="-mb-px flex space-x-8" aria-label="Tabs">
          {tabs.map((tab) => {
            const Icon = tab.icon;
            const isActive =
              location.pathname === tab.href ||
              (tab.href === '/admin/payroll' && location.pathname === '/admin/payroll');
            return (
              <Link
                key={tab.name}
                to={tab.href}
                className={cn(
                  'flex items-center gap-2 border-b-2 px-1 py-4 text-sm font-medium',
                  isActive
                    ? 'border-primary text-primary'
                    : 'border-transparent text-muted-foreground hover:border-border hover:text-foreground'
                )}
              >
                <Icon className="h-4 w-4" />
                {tab.name}
              </Link>
            );
          })}
        </nav>
      </div>

      <Outlet />
    </div>
  );
};

export default PayrollManagementPage;

