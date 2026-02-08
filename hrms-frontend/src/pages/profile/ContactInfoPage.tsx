import { useState, useEffect } from 'react';
import { useContactInfo, useUpdateContactInfo } from '@/hooks/useEmployee';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Skeleton } from '@/components/ui/skeleton';
import { PageError } from '@/components/common';
import { Loader2, Edit, X, Save } from 'lucide-react';
import type { UpdateContactInfoRequest } from '@/types';

const ContactInfoSkeleton = () => (
  <Card>
    <CardHeader>
      <Skeleton className="h-6 w-48" />
      <Skeleton className="mt-2 h-4 w-64" />
    </CardHeader>
    <CardContent className="space-y-4">
      {[1, 2, 3, 4, 5, 6].map((i) => (
        <div key={i}>
          <Skeleton className="h-4 w-24 mb-2" />
          <Skeleton className="h-10 w-full" />
        </div>
      ))}
    </CardContent>
  </Card>
);

export const ContactInfoPage = () => {
  const { data: contactInfo, isLoading, isError, error, refetch } = useContactInfo();
  const updateMutation = useUpdateContactInfo();
  const [isEditing, setIsEditing] = useState(false);
  const [formData, setFormData] = useState<UpdateContactInfoRequest>({});

  useEffect(() => {
    if (contactInfo) {
      setFormData({
        phoneNumber: contactInfo.phoneNumber || '',
        mobileNumber: contactInfo.mobileNumber || '',
        emergencyContactName: contactInfo.emergencyContactName || '',
        emergencyContactPhone: contactInfo.emergencyContactPhone || '',
        addressLine1: contactInfo.address?.line1 || '',
        addressLine2: contactInfo.address?.line2 || '',
        city: contactInfo.address?.city || '',
        state: contactInfo.address?.state || '',
        postalCode: contactInfo.address?.postalCode || '',
        country: contactInfo.address?.country || '',
      });
    }
  }, [contactInfo]);

  if (isLoading) return <ContactInfoSkeleton />;

  if (isError) {
    return (
      <PageError
        title="Failed to load contact info"
        message={error?.response?.data?.message || 'Unable to load contact information.'}
        onRetry={() => refetch()}
      />
    );
  }

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData((prev) => ({ ...prev, [e.target.name]: e.target.value }));
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    updateMutation.mutate(formData, {
      onSuccess: () => setIsEditing(false),
    });
  };

  const handleCancel = () => {
    setIsEditing(false);
    if (contactInfo) {
      setFormData({
        phoneNumber: contactInfo.phoneNumber || '',
        mobileNumber: contactInfo.mobileNumber || '',
        emergencyContactName: contactInfo.emergencyContactName || '',
        emergencyContactPhone: contactInfo.emergencyContactPhone || '',
        addressLine1: contactInfo.address?.line1 || '',
        addressLine2: contactInfo.address?.line2 || '',
        city: contactInfo.address?.city || '',
        state: contactInfo.address?.state || '',
        postalCode: contactInfo.address?.postalCode || '',
        country: contactInfo.address?.country || '',
      });
    }
  };

  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between">
        <div>
          <CardTitle>Contact Information</CardTitle>
          <CardDescription>Your contact details and address</CardDescription>
        </div>
        {!isEditing ? (
          <Button variant="outline" size="sm" onClick={() => setIsEditing(true)}>
            <Edit className="mr-2 h-4 w-4" /> Edit
          </Button>
        ) : (
          <div className="flex gap-2">
            <Button variant="outline" size="sm" onClick={handleCancel}>
              <X className="mr-2 h-4 w-4" /> Cancel
            </Button>
          </div>
        )}
      </CardHeader>
      <CardContent>
        <form onSubmit={handleSubmit} className="space-y-6">
          <div className="grid gap-4 md:grid-cols-2">
            <div className="space-y-2">
              <Label htmlFor="phoneNumber">Phone Number</Label>
              <Input
                id="phoneNumber"
                name="phoneNumber"
                value={formData.phoneNumber}
                onChange={handleChange}
                disabled={!isEditing || updateMutation.isPending}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="mobileNumber">Mobile Number</Label>
              <Input
                id="mobileNumber"
                name="mobileNumber"
                value={formData.mobileNumber}
                onChange={handleChange}
                disabled={!isEditing || updateMutation.isPending}
              />
            </div>
          </div>

          <div className="grid gap-4 md:grid-cols-2">
            <div className="space-y-2">
              <Label htmlFor="emergencyContactName">Emergency Contact Name</Label>
              <Input
                id="emergencyContactName"
                name="emergencyContactName"
                value={formData.emergencyContactName}
                onChange={handleChange}
                disabled={!isEditing || updateMutation.isPending}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="emergencyContactPhone">Emergency Contact Phone</Label>
              <Input
                id="emergencyContactPhone"
                name="emergencyContactPhone"
                value={formData.emergencyContactPhone}
                onChange={handleChange}
                disabled={!isEditing || updateMutation.isPending}
              />
            </div>
          </div>

          <div className="space-y-4">
            <h4 className="font-medium">Address</h4>
            <div className="grid gap-4 md:grid-cols-2">
              <div className="space-y-2">
                <Label htmlFor="addressLine1">Address Line 1</Label>
                <Input id="addressLine1" name="addressLine1" value={formData.addressLine1} onChange={handleChange} disabled={!isEditing || updateMutation.isPending} />
              </div>
              <div className="space-y-2">
                <Label htmlFor="addressLine2">Address Line 2</Label>
                <Input id="addressLine2" name="addressLine2" value={formData.addressLine2} onChange={handleChange} disabled={!isEditing || updateMutation.isPending} />
              </div>
              <div className="space-y-2">
                <Label htmlFor="city">City</Label>
                <Input id="city" name="city" value={formData.city} onChange={handleChange} disabled={!isEditing || updateMutation.isPending} />
              </div>
              <div className="space-y-2">
                <Label htmlFor="state">State</Label>
                <Input id="state" name="state" value={formData.state} onChange={handleChange} disabled={!isEditing || updateMutation.isPending} />
              </div>
              <div className="space-y-2">
                <Label htmlFor="postalCode">Postal Code</Label>
                <Input id="postalCode" name="postalCode" value={formData.postalCode} onChange={handleChange} disabled={!isEditing || updateMutation.isPending} />
              </div>
              <div className="space-y-2">
                <Label htmlFor="country">Country</Label>
                <Input id="country" name="country" value={formData.country} onChange={handleChange} disabled={!isEditing || updateMutation.isPending} />
              </div>
            </div>
          </div>

          {isEditing && (
            <Button type="submit" disabled={updateMutation.isPending}>
              {updateMutation.isPending ? (<><Loader2 className="mr-2 h-4 w-4 animate-spin" /> Saving...</>) : (<><Save className="mr-2 h-4 w-4" /> Save Changes</>)}
            </Button>
          )}
        </form>
      </CardContent>
    </Card>
  );
};

export default ContactInfoPage;

