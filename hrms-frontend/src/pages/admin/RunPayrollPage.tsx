import { useState } from 'react';
import { usePayrollPreview, useRunPayroll, useCurrentWeekStatus } from '@/hooks/useAdmin';
import { Card, CardContent, CardHeader, CardTitle, CardDescription, CardFooter } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';

import { Skeleton } from '@/components/ui/skeleton';
import { PageError, EmptyState } from '@/components/common';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { DollarSign, Calendar, Users, Loader2, PlayCircle, CheckCircle, AlertCircle } from 'lucide-react';

const formatCurrency = (amount: number) => {
  return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(amount);
};

export const RunPayrollPage = () => {
  const [weekStartDate, setWeekStartDate] = useState(() => {
    const today = new Date();
    const dayOfWeek = today.getDay();
    const diff = today.getDate() - dayOfWeek + (dayOfWeek === 0 ? -6 : 1); // Monday
    const monday = new Date(today.setDate(diff));
    return monday.toISOString().split('T')[0];
  });
  const [paymentDate, setPaymentDate] = useState(() => {
    const friday = new Date();
    friday.setDate(friday.getDate() + (5 - friday.getDay() + 7) % 7 + 7);
    return friday.toISOString().split('T')[0];
  });

  const { data: preview, isLoading: previewLoading, isError: previewError, error: previewErrorData, refetch: refetchPreview } = usePayrollPreview(weekStartDate);
  const { data: weekStatus } = useCurrentWeekStatus();
  const runPayrollMutation = useRunPayroll();

  const handleRunPayroll = () => {
    runPayrollMutation.mutate({ weekStartDate, paymentDate });
  };

  const isAlreadyProcessed = weekStatus?.processed;

  return (
    <div className="space-y-6">
      {/* Week Status Card */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Calendar className="h-5 w-5" /> Payroll Period
          </CardTitle>
          <CardDescription>Select the week to process payroll</CardDescription>
        </CardHeader>
        <CardContent className="grid gap-4 md:grid-cols-2">
          <div className="space-y-2">
            <Label htmlFor="weekStartDate">Week Start Date (Monday)</Label>
            <Input id="weekStartDate" type="date" value={weekStartDate} onChange={(e) => setWeekStartDate(e.target.value)} />
          </div>
          <div className="space-y-2">
            <Label htmlFor="paymentDate">Payment Date</Label>
            <Input id="paymentDate" type="date" value={paymentDate} onChange={(e) => setPaymentDate(e.target.value)} />
          </div>
        </CardContent>
      </Card>

      {/* Status Alert */}
      {weekStatus && (
        <Alert variant={weekStatus.processed ? 'default' : 'default'}>
          {weekStatus.processed ? <CheckCircle className="h-4 w-4 text-green-500" /> : <AlertCircle className="h-4 w-4" />}
          <AlertDescription>
            {weekStatus.processed ? (
              <span className="text-green-600">Payroll for this week has already been processed. Total: {formatCurrency(weekStatus.totalAmount || 0)}</span>
            ) : (
              <span>{weekStatus.employeeCount} employees ready for payroll processing.</span>
            )}
          </AlertDescription>
        </Alert>
      )}

      {/* Preview Card */}
      {previewLoading ? (
        <Card><CardContent className="p-4"><div className="space-y-4">{[1, 2, 3, 4, 5].map((i) => (<Skeleton key={i} className="h-12 w-full" />))}</div></CardContent></Card>
      ) : previewError ? (
        <PageError title="Failed to load preview" message={previewErrorData?.response?.data?.message || 'Unable to load payroll preview.'} onRetry={() => refetchPreview()} />
      ) : !preview || preview.employees.length === 0 ? (
        <EmptyState icon={DollarSign} title="No employees to process" description="No employees with approved timesheets for this period." />
      ) : (
        <Card>
          <CardHeader>
            <div className="flex items-center justify-between">
              <div>
                <CardTitle>Payroll Preview</CardTitle>
                <CardDescription>{preview.payPeriod.startDate} - {preview.payPeriod.endDate}</CardDescription>
              </div>
              <div className="text-right">
                <div className="flex items-center gap-2"><Users className="h-4 w-4 text-muted-foreground" /><span className="text-sm text-muted-foreground">{preview.employeeCount} employees</span></div>
              </div>
            </div>
          </CardHeader>
          <CardContent>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Employee</TableHead>
                  <TableHead className="text-right">Hours</TableHead>
                  <TableHead className="text-right">Gross Pay</TableHead>
                  <TableHead className="text-right">Tax</TableHead>
                  <TableHead className="text-right">Other</TableHead>
                  <TableHead className="text-right">Bonus</TableHead>
                  <TableHead className="text-right">Net Pay</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {preview.employees.map((emp) => (
                  <TableRow key={emp.employeeId}>
                    <TableCell className="font-medium">{emp.name}</TableCell>
                    <TableCell className="text-right">{emp.hoursWorked.toFixed(1)}</TableCell>
                    <TableCell className="text-right">{formatCurrency(emp.grossPay)}</TableCell>
                    <TableCell className="text-right text-red-500">-{formatCurrency(emp.taxDeduction)}</TableCell>
                    <TableCell className="text-right text-red-500">-{formatCurrency(emp.otherDeductions)}</TableCell>
                    <TableCell className="text-right text-green-500">+{formatCurrency(emp.bonus)}</TableCell>
                    <TableCell className="text-right font-semibold">{formatCurrency(emp.netPay)}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </CardContent>
          <CardFooter className="flex justify-between border-t pt-6">
            <div className="space-y-1">
              <p className="text-sm text-muted-foreground">Total Gross: {formatCurrency(preview.totalGrossPay)}</p>
              <p className="text-lg font-bold">Total Net: {formatCurrency(preview.totalNetPay)}</p>
            </div>
            <Button onClick={handleRunPayroll} disabled={runPayrollMutation.isPending || isAlreadyProcessed} size="lg">
              {runPayrollMutation.isPending ? <Loader2 className="mr-2 h-5 w-5 animate-spin" /> : <PlayCircle className="mr-2 h-5 w-5" />}
              {isAlreadyProcessed ? 'Already Processed' : 'Run Payroll'}
            </Button>
          </CardFooter>
        </Card>
      )}
    </div>
  );
};

export default RunPayrollPage;

