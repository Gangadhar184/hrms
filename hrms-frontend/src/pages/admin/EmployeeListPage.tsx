import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useAllEmployees, useEmployeeStatistics } from '@/hooks/useAdmin';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Badge } from '@/components/ui/badge';
import { Skeleton } from '@/components/ui/skeleton';
import { Select, SelectOption } from '@/components/ui/select';
import { PageHeader, PageError, EmptyState } from '@/components/common';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Users, Search, Plus, Eye, UserCog, UserCheck, Shield } from 'lucide-react';
import { Role } from '@/types';

export const EmployeeListPage = () => {
  const [search, setSearch] = useState('');
  const [roleFilter, setRoleFilter] = useState<Role | ''>('');
  const [page, setPage] = useState(0);

  const { data: employees, isLoading, isError, error, refetch } = useAllEmployees({
    search: search || undefined,
    role: roleFilter || undefined,
    page,
    size: 15,
  });

  const { data: stats, isLoading: statsLoading } = useEmployeeStatistics();

  const formatDate = (dateStr: string) => {
    return new Date(dateStr).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
  };

  const getRoleBadge = (role: Role) => {
    switch (role) {
      case Role.ADMIN:
        return <Badge variant="destructive"><Shield className="mr-1 h-3 w-3" />{role}</Badge>;
      case Role.MANAGER:
        return <Badge variant="default"><UserCog className="mr-1 h-3 w-3" />{role}</Badge>;
      default:
        return <Badge variant="secondary"><UserCheck className="mr-1 h-3 w-3" />{role}</Badge>;
    }
  };

  return (
    <div className="space-y-6">
      <PageHeader title="Employee Management" description="View and manage all employees in the organization">
        <Button asChild>
          <Link to="/admin/employees/create"><Plus className="mr-2 h-4 w-4" />Add Employee</Link>
        </Button>
      </PageHeader>

      {/* Stats Cards */}
      <div className="grid gap-4 md:grid-cols-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium">Total Employees</CardTitle>
            <Users className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            {statsLoading ? <Skeleton className="h-8 w-16" /> : <div className="text-2xl font-bold">{stats?.totalEmployees || 0}</div>}
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium">Admins</CardTitle>
            <Shield className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            {statsLoading ? <Skeleton className="h-8 w-16" /> : <div className="text-2xl font-bold">{stats?.totalAdmins || 0}</div>}
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium">Managers</CardTitle>
            <UserCog className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            {statsLoading ? <Skeleton className="h-8 w-16" /> : <div className="text-2xl font-bold">{stats?.totalManagers || 0}</div>}
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium">Employees</CardTitle>
            <UserCheck className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            {statsLoading ? <Skeleton className="h-8 w-16" /> : <div className="text-2xl font-bold">{stats?.totalRegularEmployees || 0}</div>}
          </CardContent>
        </Card>
      </div>

      {/* Filters */}
      <div className="flex flex-col gap-4 md:flex-row">
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
          <Input placeholder="Search by name or email..." value={search} onChange={(e) => { setSearch(e.target.value); setPage(0); }} className="pl-9" />
        </div>
        <Select value={roleFilter} onChange={(e) => { setRoleFilter(e.target.value as Role | ''); setPage(0); }} className="w-[180px]">
          <SelectOption value="">All Roles</SelectOption>
          <SelectOption value={Role.ADMIN}>Admin</SelectOption>
          <SelectOption value={Role.MANAGER}>Manager</SelectOption>
          <SelectOption value={Role.EMPLOYEE}>Employee</SelectOption>
        </Select>
      </div>

      {/* Employee Table */}
      {isLoading ? (
        <Card><CardContent className="p-4"><div className="space-y-4">{[1, 2, 3, 4, 5].map((i) => (<Skeleton key={i} className="h-12 w-full" />))}</div></CardContent></Card>
      ) : isError ? (
        <PageError title="Failed to load employees" message={error?.response?.data?.message || 'Unable to load employees.'} onRetry={() => refetch()} />
      ) : !employees?.content || employees.content.length === 0 ? (
        <EmptyState icon={Users} title="No employees found" description={search || roleFilter ? 'Try adjusting your filters.' : 'No employees in the system yet.'} action={{ label: 'Add Employee', onClick: () => {} }} />
      ) : (
        <>
          <Card>
            <CardContent className="p-0">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Employee</TableHead>
                    <TableHead>Employee ID</TableHead>
                    <TableHead>Role</TableHead>
                    <TableHead>Manager</TableHead>
                    <TableHead>Hire Date</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead className="text-right">Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {employees.content.map((emp) => (
                    <TableRow key={emp.id}>
                      <TableCell>
                        <div>
                          <p className="font-medium">{emp.firstName} {emp.lastName}</p>
                          <p className="text-xs text-muted-foreground">{emp.email}</p>
                        </div>
                      </TableCell>
                      <TableCell className="font-mono text-sm">{emp.employeeId}</TableCell>
                      <TableCell>{getRoleBadge(emp.role)}</TableCell>
                      <TableCell className="text-muted-foreground">{emp.managerName || '-'}</TableCell>
                      <TableCell className="text-muted-foreground">{formatDate(emp.hireDate)}</TableCell>
                      <TableCell><Badge variant={emp.isActive ? 'success' : 'secondary'}>{emp.isActive ? 'Active' : 'Inactive'}</Badge></TableCell>
                      <TableCell className="text-right">
                        <Button variant="ghost" size="sm" asChild><Link to={`/admin/employees/${emp.id}`}><Eye className="mr-2 h-4 w-4" />View</Link></Button>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </CardContent>
          </Card>

          {employees.totalPages > 1 && (
            <div className="flex justify-center gap-2">
              <Button variant="outline" size="sm" onClick={() => setPage((p) => Math.max(0, p - 1))} disabled={employees.first}>Previous</Button>
              <span className="flex items-center px-4 text-sm text-muted-foreground">Page {employees.currentPage + 1} of {employees.totalPages}</span>
              <Button variant="outline" size="sm" onClick={() => setPage((p) => p + 1)} disabled={employees.last}>Next</Button>
            </div>
          )}
        </>
      )}
    </div>
  );
};

export default EmployeeListPage;

