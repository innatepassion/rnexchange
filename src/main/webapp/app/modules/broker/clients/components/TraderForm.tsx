import React from 'react';
import { Form, FormGroup, Label, Input, Row, Col } from 'reactstrap';
import { ITraderProfile } from 'app/shared/model/trader-profile.model';
import { AccountStatus } from 'app/shared/model/enumerations/account-status.model';
import { KycStatus } from 'app/shared/model/enumerations/kyc-status.model';

interface TraderFormProps {
  formData: Partial<ITraderProfile>;
  users: any[];
  onChange: (data: Partial<ITraderProfile>) => void;
}

const TraderForm: React.FC<TraderFormProps> = ({ formData, users, onChange }) => {
  const handleChange = (field: keyof ITraderProfile, value: any) => {
    onChange({ ...formData, [field]: value });
  };

  return (
    <Form>
      <Row>
        <Col md="6">
          <FormGroup>
            <Label>Display Name *</Label>
            <Input type="text" value={formData.displayName || ''} onChange={e => handleChange('displayName', e.target.value)} required />
          </FormGroup>
        </Col>
        <Col md="6">
          <FormGroup>
            <Label>Email *</Label>
            <Input type="email" value={formData.email || ''} onChange={e => handleChange('email', e.target.value)} required />
          </FormGroup>
        </Col>
      </Row>
      <Row>
        <Col md="6">
          <FormGroup>
            <Label>Mobile</Label>
            <Input type="text" value={formData.mobile || ''} onChange={e => handleChange('mobile', e.target.value)} />
          </FormGroup>
        </Col>
        <Col md="6">
          <FormGroup>
            <Label>KYC Status *</Label>
            <Input
              type="select"
              value={formData.kycStatus || 'PENDING'}
              onChange={e => handleChange('kycStatus', e.target.value as keyof typeof KycStatus)}
            >
              {Object.keys(KycStatus).map(status => (
                <option key={status} value={status}>
                  {status}
                </option>
              ))}
            </Input>
          </FormGroup>
        </Col>
      </Row>
      <Row>
        <Col md="6">
          <FormGroup>
            <Label>Status *</Label>
            <Input
              type="select"
              value={formData.status || 'ACTIVE'}
              onChange={e => handleChange('status', e.target.value as keyof typeof AccountStatus)}
            >
              {Object.keys(AccountStatus).map(status => (
                <option key={status} value={status}>
                  {status}
                </option>
              ))}
            </Input>
          </FormGroup>
        </Col>
        <Col md="6">
          <FormGroup>
            <Label>User</Label>
            <Input
              type="select"
              value={formData.user?.id?.toString() || ''}
              onChange={e => handleChange('user', users.find(u => u.id.toString() === e.target.value) || null)}
            >
              <option value="">Select a user</option>
              {users.map(user => (
                <option key={user.id} value={user.id}>
                  {user.login}
                </option>
              ))}
            </Input>
          </FormGroup>
        </Col>
      </Row>
    </Form>
  );
};

export default TraderForm;
