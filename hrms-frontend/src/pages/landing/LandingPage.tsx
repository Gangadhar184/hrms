import { Link } from 'react-router-dom';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import {
  Clock,
  Users,
  Calendar,
  CheckCircle,
  Shield,
  BarChart3,
  ArrowRight,
  Building2,
  UserCheck,
  FileText,
} from 'lucide-react';

// Feature data
const features = [
  {
    icon: Clock,
    title: 'Timesheet Management',
    description: 'Track work hours with weekly timesheets. Submit, save drafts, and manage your time efficiently.',
  },
  {
    icon: Calendar,
    title: 'Leave Management',
    description: 'Request time off, track leave balances, and manage approvals all in one place.',
  },
  {
    icon: Users,
    title: 'Employee Management',
    description: 'Comprehensive employee profiles, department organization, and hierarchy management.',
  },
  {
    icon: CheckCircle,
    title: 'Manager Approvals',
    description: 'Streamlined approval workflows for timesheets and leave requests with feedback.',
  },
];

// Benefits by role
const benefits = [
  {
    role: 'Employees',
    icon: UserCheck,
    items: [
      'Easy timesheet submission',
      'Track leave balances',
      'View payroll history',
      'Update personal information',
    ],
  },
  {
    role: 'Managers',
    icon: BarChart3,
    items: [
      'Review team timesheets',
      'Approve/deny with feedback',
      'Monitor team statistics',
      'Manage direct reports',
    ],
  },
  {
    role: 'Administrators',
    icon: Shield,
    items: [
      'Full employee management',
      'Run payroll processing',
      'System-wide oversight',
      'Create new employees',
    ],
  },
];

export const LandingPage = () => {
  return (
    <div className="min-h-screen bg-white">
      {/* Navigation */}
      <nav className="sticky top-0 z-50 border-b border-gray-100 bg-white/80 backdrop-blur-md">
        <div className="mx-auto flex h-16 max-w-7xl items-center justify-between px-4 sm:px-6 lg:px-8">
          <div className="flex items-center gap-2">
            <Building2 className="h-8 w-8 text-gray-900" />
            <span className="text-xl font-bold text-gray-900">HRMS</span>
          </div>
          <div className="flex items-center gap-4">
            <Link to="/login">
              <Button variant="ghost" className="text-gray-600 hover:text-gray-900">
                Login
              </Button>
            </Link>
            <Link to="/login">
              <Button className="bg-gray-900 text-white hover:bg-gray-800">
                Get Started
              </Button>
            </Link>
          </div>
        </div>
      </nav>

      {/* Hero Section */}
      <section className="relative overflow-hidden bg-gradient-to-b from-gray-50 to-white px-4 py-20 sm:px-6 sm:py-32 lg:px-8">
        <div className="absolute inset-0 -z-10">
          <div className="absolute left-1/2 top-0 h-[500px] w-[500px] -translate-x-1/2 rounded-full bg-gray-100/50 blur-3xl" />
        </div>
        <div className="mx-auto max-w-4xl text-center">
          <h1 className="text-4xl font-bold tracking-tight text-gray-900 sm:text-5xl lg:text-6xl">
            Modern HR Management
            <span className="block text-gray-500">Made Simple</span>
          </h1>
          <p className="mx-auto mt-6 max-w-2xl text-lg text-gray-600 sm:text-xl">
            Streamline your workforce management with our comprehensive HRMS solution. 
            Track timesheets, manage leaves, process payroll, and empower your team.
          </p>
          <div className="mt-10 flex flex-col items-center justify-center gap-4 sm:flex-row">
            <Link to="/login">
              <Button size="lg" className="w-full bg-gray-900 text-white hover:bg-gray-800 sm:w-auto">
                Get Started <ArrowRight className="ml-2 h-4 w-4" />
              </Button>
            </Link>
            <Link to="/login">
              <Button size="lg" variant="outline" className="w-full border-gray-300 sm:w-auto">
                <FileText className="mr-2 h-4 w-4" /> View Demo
              </Button>
            </Link>
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section className="bg-white px-4 py-20 sm:px-6 lg:px-8">
        <div className="mx-auto max-w-7xl">
          <div className="text-center">
            <h2 className="text-3xl font-bold text-gray-900 sm:text-4xl">
              Everything You Need
            </h2>
            <p className="mx-auto mt-4 max-w-2xl text-gray-600">
              Powerful features to manage your entire HR workflow efficiently
            </p>
          </div>
          <div className="mt-16 grid gap-8 sm:grid-cols-2 lg:grid-cols-4">
            {features.map((feature) => (
              <Card key={feature.title} className="border-gray-100 bg-gray-50/50 transition-all hover:border-gray-200 hover:shadow-md">
                <CardHeader>
                  <div className="mb-2 flex h-12 w-12 items-center justify-center rounded-lg bg-gray-900">
                    <feature.icon className="h-6 w-6 text-white" />
                  </div>
                  <CardTitle className="text-gray-900">{feature.title}</CardTitle>
                </CardHeader>
                <CardContent>
                  <CardDescription className="text-gray-600">{feature.description}</CardDescription>
                </CardContent>
              </Card>
            ))}
          </div>
        </div>
      </section>

      {/* Benefits Section */}
      <section className="bg-gray-50 px-4 py-20 sm:px-6 lg:px-8">
        <div className="mx-auto max-w-7xl">
          <div className="text-center">
            <h2 className="text-3xl font-bold text-gray-900 sm:text-4xl">
              Built for Every Role
            </h2>
            <p className="mx-auto mt-4 max-w-2xl text-gray-600">
              Tailored experiences for employees, managers, and administrators
            </p>
          </div>
          <div className="mt-16 grid gap-8 md:grid-cols-3">
            {benefits.map((benefit) => (
              <Card key={benefit.role} className="border-gray-200 bg-white">
                <CardHeader>
                  <div className="mb-2 flex h-10 w-10 items-center justify-center rounded-full bg-gray-100">
                    <benefit.icon className="h-5 w-5 text-gray-700" />
                  </div>
                  <CardTitle className="text-xl text-gray-900">{benefit.role}</CardTitle>
                </CardHeader>
                <CardContent>
                  <ul className="space-y-3">
                    {benefit.items.map((item) => (
                      <li key={item} className="flex items-center gap-3 text-gray-600">
                        <CheckCircle className="h-4 w-4 flex-shrink-0 text-gray-400" />
                        <span>{item}</span>
                      </li>
                    ))}
                  </ul>
                </CardContent>
              </Card>
            ))}
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="bg-gray-900 px-4 py-20 sm:px-6 lg:px-8">
        <div className="mx-auto max-w-4xl text-center">
          <h2 className="text-3xl font-bold text-white sm:text-4xl">
            Ready to Get Started?
          </h2>
          <p className="mx-auto mt-4 max-w-2xl text-gray-400">
            Join thousands of companies streamlining their HR processes with HRMS
          </p>
          <div className="mt-10">
            <Link to="/login">
              <Button size="lg" className="bg-white text-gray-900 hover:bg-gray-100">
                Start Now <ArrowRight className="ml-2 h-4 w-4" />
              </Button>
            </Link>
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="border-t border-gray-100 bg-white px-4 py-12 sm:px-6 lg:px-8">
        <div className="mx-auto max-w-7xl">
          <div className="flex flex-col items-center justify-between gap-6 md:flex-row">
            <div className="flex items-center gap-2">
              <Building2 className="h-6 w-6 text-gray-900" />
              <span className="font-semibold text-gray-900">HRMS</span>
            </div>
            <div className="flex flex-wrap items-center justify-center gap-6 text-sm text-gray-600">
              <a href="#" className="hover:text-gray-900">About</a>
              <a href="#" className="hover:text-gray-900">Features</a>
              <a href="#" className="hover:text-gray-900">Documentation</a>
              <a href="#" className="hover:text-gray-900">Support</a>
            </div>
            <p className="text-sm text-gray-500">
              © {new Date().getFullYear()} HRMS. All rights reserved.
            </p>
          </div>
        </div>
      </footer>
    </div>
  );
};

export default LandingPage;

