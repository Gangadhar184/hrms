import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useCreateEmployee, useActiveManagers } from '@/hooks/useAdmin';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Select, SelectOption } from '@/components/ui/select';
import { PageHeader } from '@/components/common';
import { ArrowLeft, Loader2, UserPlus } from 'lucide-react';
import { Role, PayFrequency, PaymentMethod, type CreateEmployeeRequest } from '@/types';

export const CreateEmployeePage = () => {
  const navigate = useNavigate();
  const createMutation = useCreateEmployee();
  const { data: managers } = useActiveManagers();

  const [formData, setFormData] = useState<CreateEmployeeRequest>({
    username: '',
    email: '',
    firstName: '',
    lastName: '',
    dateOfBirth: '',
    hireDate: new Date().toISOString().split('T')[0],
    role: Role.EMPLOYEE,
    managerId: undefined,
    payInfo: {
      salary: 0,
      hourlyRate: undefined,
      payFrequency: PayFrequency.BI_WEEKLY,
      paymentMethod: PaymentMethod.BANK_TRANSFER,
      bankName: '',
      accountNumber: '',
      routingNumber: '',
      taxId: '',
    },
  });

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    if (name.startsWith('payInfo.')) {
      const payField = name.replace('payInfo.', '');
      setFormData((prev) => ({
        ...prev,
        payInfo: { ...prev.payInfo!, [payField]: payField === 'salary' || payField === 'hourlyRate' ? parseFloat(value) || 0 : value },
      }));
    } else if (name === 'managerId') {
      setFormData((prev) => ({ ...prev, [name]: value ? parseInt(value) : undefined }));
    } else {
      setFormData((prev) => ({ ...prev, [name]: value }));
    }
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    createMutation.mutate(formData, {
      onSuccess: () => navigate('/admin/employees'),
    });
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="sm" asChild>
          <Link to="/admin/employees"><ArrowLeft className="mr-2 h-4 w-4" />Back to Employees</Link>
        </Button>
      </div>

      <PageHeader title="Create New Employee" description="Add a new employee to the organization" />

      <form onSubmit={handleSubmit} className="space-y-6">
        <Card>
          <CardHeader><CardTitle>Basic Information</CardTitle><CardDescription>Enter the employee's personal details</CardDescription></CardHeader>
          <CardContent className="grid gap-4 md:grid-cols-2">
            <div className="space-y-2"><Label htmlFor="firstName">First Name *</Label><Input id="firstName" name="firstName" value={formData.firstName} onChange={handleChange} required /></div>
            <div className="space-y-2"><Label htmlFor="lastName">Last Name *</Label><Input id="lastName" name="lastName" value={formData.lastName} onChange={handleChange} required /></div>
            <div className="space-y-2"><Label htmlFor="username">Username *</Label><Input id="username" name="username" value={formData.username} onChange={handleChange} required /></div>
            <div className="space-y-2"><Label htmlFor="email">Email *</Label><Input id="email" name="email" type="email" value={formData.email} onChange={handleChange} required /></div>
            <div className="space-y-2"><Label htmlFor="dateOfBirth">Date of Birth</Label><Input id="dateOfBirth" name="dateOfBirth" type="date" value={formData.dateOfBirth} onChange={handleChange} /></div>
            <div className="space-y-2"><Label htmlFor="hireDate">Hire Date *</Label><Input id="hireDate" name="hireDate" type="date" value={formData.hireDate} onChange={handleChange} required /></div>
            <div className="space-y-2"><Label htmlFor="role">Role *</Label><Select id="role" name="role" value={formData.role} onChange={handleChange}><SelectOption value={Role.EMPLOYEE}>Employee</SelectOption><SelectOption value={Role.MANAGER}>Manager</SelectOption><SelectOption value={Role.ADMIN}>Admin</SelectOption></Select></div>
            <div className="space-y-2"><Label htmlFor="managerId">Manager</Label><Select id="managerId" name="managerId" value={formData.managerId?.toString() || ''} onChange={handleChange}><SelectOption value="">No Manager</SelectOption>{managers?.map((m) => (<SelectOption key={m.id} value={m.id.toString()}>{m.firstName} {m.lastName}</SelectOption>))}</Select></div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader><CardTitle>Pay Information</CardTitle><CardDescription>Set up the employee's compensation</CardDescription></CardHeader>
          <CardContent className="grid gap-4 md:grid-cols-2">
            <div className="space-y-2"><Label htmlFor="payInfo.salary">Annual Salary *</Label><Input id="payInfo.salary" name="payInfo.salary" type="number" min="0" step="0.01" value={formData.payInfo?.salary || ''} onChange={handleChange} required /></div>
            <div className="space-y-2"><Label htmlFor="payInfo.hourlyRate">Hourly Rate</Label><Input id="payInfo.hourlyRate" name="payInfo.hourlyRate" type="number" min="0" step="0.01" value={formData.payInfo?.hourlyRate || ''} onChange={handleChange} /></div>
            <div className="space-y-2"><Label htmlFor="payInfo.payFrequency">Pay Frequency *</Label><Select id="payInfo.payFrequency" name="payInfo.payFrequency" value={formData.payInfo?.payFrequency} onChange={handleChange}><SelectOption value={PayFrequency.WEEKLY}>Weekly</SelectOption><SelectOption value={PayFrequency.BI_WEEKLY}>Bi-Weekly</SelectOption><SelectOption value={PayFrequency.SEMI_MONTHLY}>Semi-Monthly</SelectOption><SelectOption value={PayFrequency.MONTHLY}>Monthly</SelectOption></Select></div>
            <div className="space-y-2"><Label htmlFor="payInfo.paymentMethod">Payment Method *</Label><Select id="payInfo.paymentMethod" name="payInfo.paymentMethod" value={formData.payInfo?.paymentMethod} onChange={handleChange}><SelectOption value={PaymentMethod.BANK_TRANSFER}>Bank Transfer</SelectOption><SelectOption value={PaymentMethod.CHECK}>Check</SelectOption><SelectOption value={PaymentMethod.CASH}>Cash</SelectOption><SelectOption value={PaymentMethod.DIGITAL_WALLET}>Digital Wallet</SelectOption></Select></div>
            <div className="space-y-2"><Label htmlFor="payInfo.bankName">Bank Name</Label><Input id="payInfo.bankName" name="payInfo.bankName" value={formData.payInfo?.bankName || ''} onChange={handleChange} /></div>
            <div className="space-y-2"><Label htmlFor="payInfo.accountNumber">Account Number</Label><Input id="payInfo.accountNumber" name="payInfo.accountNumber" value={formData.payInfo?.accountNumber || ''} onChange={handleChange} /></div>
            <div className="space-y-2"><Label htmlFor="payInfo.routingNumber">Routing Number</Label><Input id="payInfo.routingNumber" name="payInfo.routingNumber" value={formData.payInfo?.routingNumber || ''} onChange={handleChange} /></div>
            <div className="space-y-2"><Label htmlFor="payInfo.taxId">Tax ID</Label><Input id="payInfo.taxId" name="payInfo.taxId" value={formData.payInfo?.taxId || ''} onChange={handleChange} /></div>
          </CardContent>
        </Card>

        <div className="flex justify-end gap-4">
          <Button type="button" variant="outline" onClick={() => navigate('/admin/employees')}>Cancel</Button>
          <Button type="submit" disabled={createMutation.isPending}>
            {createMutation.isPending ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : <UserPlus className="mr-2 h-4 w-4" />}Create Employee
          </Button>
        </div>
      </form>
    </div>
  );
};

export default CreateEmployeePage;

