import { NavLink, useLocation } from 'react-router-dom';
import {
  LayoutDashboard,
  User,
  Clock,
  DollarSign,
  Users,
  ClipboardList,
  BarChart3,
  UserPlus,
  ChevronLeft,
  ChevronRight,
} from 'lucide-react';
import { cn } from '@/lib/utils';
import { useAuth } from '@/contexts/AuthContext';
import { Role } from '@/types';
import { Button } from '@/components/ui/button';

interface NavItem {
  title: string;
  href: string;
  icon: React.ElementType;
  roles?: Role[];
}

const navItems: NavItem[] = [
  {
    title: 'Dashboard',
    href: '/dashboard',
    icon: LayoutDashboard,
  },
  {
    title: 'My Profile',
    href: '/profile',
    icon: User,
  },
  {
    title: 'Timesheets',
    href: '/timesheets',
    icon: Clock,
  },
  {
    title: 'Payroll',
    href: '/payroll',
    icon: DollarSign,
  },
  // Manager-specific items
  {
    title: 'Direct Reports',
    href: '/manager/team',
    icon: Users,
    roles: [Role.MANAGER, Role.ADMIN],
  },
  {
    title: 'Team Timesheets',
    href: '/manager/timesheets',
    icon: ClipboardList,
    roles: [Role.MANAGER, Role.ADMIN],
  },
  {
    title: 'Statistics',
    href: '/manager/statistics',
    icon: BarChart3,
    roles: [Role.MANAGER, Role.ADMIN],
  },
  // Admin-specific items
  {
    title: 'Employees',
    href: '/admin/employees',
    icon: Users,
    roles: [Role.ADMIN],
  },
  {
    title: 'Create Employee',
    href: '/admin/employees/create',
    icon: UserPlus,
    roles: [Role.ADMIN],
  },
  {
    title: 'Payroll Management',
    href: '/admin/payroll',
    icon: DollarSign,
    roles: [Role.ADMIN],
  },
];

interface SidebarProps {
  isCollapsed: boolean;
  onToggle: () => void;
}

export const Sidebar = ({ isCollapsed, onToggle }: SidebarProps) => {
  const { hasRole } = useAuth();
  const location = useLocation();

  const filteredNavItems = navItems.filter((item) => {
    if (!item.roles) return true;
    return hasRole(item.roles);
  });

  return (
    <aside
      className={cn(
        'fixed left-0 top-0 z-40 h-screen border-r bg-sidebar transition-all duration-300',
        isCollapsed ? 'w-16' : 'w-64'
      )}
    >
      <div className="flex h-16 items-center justify-between border-b px-4">
        {!isCollapsed && (
          <span className="text-xl font-bold text-sidebar-primary">HRMS</span>
        )}
        <Button
          variant="ghost"
          size="icon-sm"
          onClick={onToggle}
          className="ml-auto"
        >
          {isCollapsed ? (
            <ChevronRight className="h-4 w-4" />
          ) : (
            <ChevronLeft className="h-4 w-4" />
          )}
        </Button>
      </div>

      <nav className="flex flex-col gap-1 p-2">
        {filteredNavItems.map((item) => {
          const Icon = item.icon;
          const isActive = location.pathname === item.href ||
            location.pathname.startsWith(item.href + '/');

          return (
            <NavLink
              key={item.href}
              to={item.href}
              className={cn(
                'flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium transition-colors',
                'hover:bg-sidebar-accent hover:text-sidebar-accent-foreground',
                isActive
                  ? 'bg-sidebar-accent text-sidebar-accent-foreground'
                  : 'text-sidebar-foreground',
                isCollapsed && 'justify-center px-2'
              )}
              title={isCollapsed ? item.title : undefined}
            >
              <Icon className="h-5 w-5 shrink-0" />
              {!isCollapsed && <span>{item.title}</span>}
            </NavLink>
          );
        })}
      </nav>
    </aside>
  );
};

export default Sidebar;

