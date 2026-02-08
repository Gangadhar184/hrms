import { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { useEmployeeById, useUpdateEmployeePersonalInfo, useActiveManagers } from '@/hooks/useAdmin';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Badge } from '@/components/ui/badge';
import { Skeleton } from '@/components/ui/skeleton';
import { Select, SelectOption } from '@/components/ui/select';
import { PageError } from '@/components/common';
import { ArrowLeft, Edit, Save, X, Loader2 } from 'lucide-react';
import type { UpdateEmployeePersonalInfoRequest } from '@/types';

const DetailSkeleton = () => (
  <div className="space-y-6">
    <Skeleton className="h-8 w-32" />
    <Card><CardHeader><Skeleton className="h-6 w-48" /></CardHeader><CardContent className="space-y-4">{[1, 2, 3, 4, 5].map((i) => (<Skeleton key={i} className="h-10 w-full" />))}</CardContent></Card>
  </div>
);

export const EmployeeDetailPage = () => {
  const { id } = useParams<{ id: string }>();
  const employeeId = id ? parseInt(id) : undefined;
  const { data: employee, isLoading, isError, error, refetch } = useEmployeeById(employeeId);
  const { data: managers } = useActiveManagers();
  const updateMutation = useUpdateEmployeePersonalInfo();

  const [isEditing, setIsEditing] = useState(false);
  const [formData, setFormData] = useState<UpdateEmployeePersonalInfoRequest>({
    firstName: '',
    lastName: '',
    dateOfBirth: '',
    email: '',
    managerId: undefined,
    isActive: true,
  });

  useEffect(() => {
    if (employee) {
      setFormData({
        firstName: employee.firstName,
        lastName: employee.lastName,
        dateOfBirth: employee.dateOfBirth || '',
        email: employee.email,
        managerId: employee.manager?.id,
        isActive: employee.isActive,
      });
    }
  }, [employee]);

  if (isLoading) return <DetailSkeleton />;

  if (isError) {
    return <PageError title="Failed to load employee" message={error?.response?.data?.message || 'Unable to load employee details.'} onRetry={() => refetch()} />;
  }

  if (!employee) return null;

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value, type } = e.target;
    if (type === 'checkbox') {
      setFormData((prev) => ({ ...prev, [name]: (e.target as HTMLInputElement).checked }));
    } else if (name === 'managerId') {
      setFormData((prev) => ({ ...prev, [name]: value ? parseInt(value) : undefined }));
    } else {
      setFormData((prev) => ({ ...prev, [name]: value }));
    }
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!employeeId) return;
    updateMutation.mutate({ employeeId, data: formData }, { onSuccess: () => setIsEditing(false) });
  };

  const handleCancel = () => {
    setIsEditing(false);
    if (employee) {
      setFormData({
        firstName: employee.firstName,
        lastName: employee.lastName,
        dateOfBirth: employee.dateOfBirth || '',
        email: employee.email,
        managerId: employee.manager?.id,
        isActive: employee.isActive,
      });
    }
  };

  const formatDate = (dateStr?: string) => {
    if (!dateStr) return 'N/A';
    return new Date(dateStr).toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' });
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="sm" asChild>
          <Link to="/admin/employees"><ArrowLeft className="mr-2 h-4 w-4" />Back to Employees</Link>
        </Button>
      </div>

      <Card>
        <CardHeader className="flex flex-row items-center justify-between">
          <div>
            <CardTitle>Employee Details</CardTitle>
            <CardDescription>ID: {employee.employeeId} | Username: @{employee.username}</CardDescription>
          </div>
          <div className="flex items-center gap-2">
            <Badge variant={employee.isActive ? 'success' : 'secondary'}>{employee.isActive ? 'Active' : 'Inactive'}</Badge>
            <Badge variant="outline">{employee.role}</Badge>
            {!isEditing && (
              <Button variant="outline" size="sm" onClick={() => setIsEditing(true)}>
                <Edit className="mr-2 h-4 w-4" />Edit
              </Button>
            )}
          </div>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-6">
            <div className="grid gap-4 md:grid-cols-2">
              <div className="space-y-2">
                <Label htmlFor="firstName">First Name</Label>
                <Input id="firstName" name="firstName" value={formData.firstName} onChange={handleChange} disabled={!isEditing || updateMutation.isPending} />
              </div>
              <div className="space-y-2">
                <Label htmlFor="lastName">Last Name</Label>
                <Input id="lastName" name="lastName" value={formData.lastName} onChange={handleChange} disabled={!isEditing || updateMutation.isPending} />
              </div>
              <div className="space-y-2">
                <Label htmlFor="email">Email</Label>
                <Input id="email" name="email" type="email" value={formData.email} onChange={handleChange} disabled={!isEditing || updateMutation.isPending} />
              </div>
              <div className="space-y-2">
                <Label htmlFor="dateOfBirth">Date of Birth</Label>
                <Input id="dateOfBirth" name="dateOfBirth" type="date" value={formData.dateOfBirth} onChange={handleChange} disabled={!isEditing || updateMutation.isPending} />
              </div>
              <div className="space-y-2">
                <Label htmlFor="managerId">Manager</Label>
                <Select id="managerId" name="managerId" value={formData.managerId?.toString() || ''} onChange={handleChange} disabled={!isEditing || updateMutation.isPending}>
                  <SelectOption value="">No Manager</SelectOption>
                  {managers?.map((m) => (
                    <SelectOption key={m.id} value={m.id.toString()}>{m.firstName} {m.lastName}</SelectOption>
                  ))}
                </Select>
              </div>
              <div className="space-y-2">
                <Label htmlFor="isActive">Status</Label>
                <Select id="isActive" name="isActive" value={formData.isActive.toString()} onChange={(e) => setFormData((prev) => ({ ...prev, isActive: e.target.value === 'true' }))} disabled={!isEditing || updateMutation.isPending}>
                  <SelectOption value="true">Active</SelectOption>
                  <SelectOption value="false">Inactive</SelectOption>
                </Select>
              </div>
            </div>

            <div className="rounded-lg border p-4">
              <h4 className="font-medium mb-2">Read-Only Information</h4>
              <div className="grid gap-4 text-sm md:grid-cols-2">
                <div><span className="text-muted-foreground">Hire Date:</span> {formatDate(employee.hireDate)}</div>
                <div><span className="text-muted-foreground">Created:</span> {formatDate(employee.createdAt)}</div>
                <div><span className="text-muted-foreground">Updated:</span> {formatDate(employee.updatedAt)}</div>
                <div><span className="text-muted-foreground">First Login:</span> {employee.isFirstLogin ? 'Yes' : 'No'}</div>
              </div>
            </div>

            {isEditing && (
              <div className="flex justify-end gap-2">
                <Button type="button" variant="outline" onClick={handleCancel}><X className="mr-2 h-4 w-4" />Cancel</Button>
                <Button type="submit" disabled={updateMutation.isPending}>
                  {updateMutation.isPending ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : <Save className="mr-2 h-4 w-4" />}Save Changes
                </Button>
              </div>
            )}
          </form>
        </CardContent>
      </Card>
    </div>
  );
};

export default EmployeeDetailPage;

