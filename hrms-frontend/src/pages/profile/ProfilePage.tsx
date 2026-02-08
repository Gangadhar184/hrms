import { Link, Outlet, useLocation } from 'react-router-dom';
import { User, Phone, DollarSign } from 'lucide-react';
import { cn } from '@/lib/utils';
import { PageHeader } from '@/components/common';

const tabs = [
  { name: 'Personal Info', href: '/profile', icon: User },
  { name: 'Contact Info', href: '/profile/contact', icon: Phone },
  { name: 'Pay Info', href: '/profile/pay', icon: DollarSign },
];

export const ProfilePage = () => {
  const location = useLocation();

  return (
    <div className="space-y-6">
      <PageHeader
        title="My Profile"
        description="View and manage your personal information"
      />

      <div className="border-b">
        <nav className="-mb-px flex space-x-8" aria-label="Tabs">
          {tabs.map((tab) => {
            const Icon = tab.icon;
            const isActive = location.pathname === tab.href;
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

export default ProfilePage;

