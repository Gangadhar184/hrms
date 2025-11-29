package com.example.hrms.mappers;

import com.example.hrms.dto.ContactInfoResponse;
import com.example.hrms.dto.UpdateContactInfoRequest;
import com.example.hrms.models.ContactInfo;
import org.springframework.stereotype.Component;

@Component
public class ContactInfoMapper {

    /**
     * Convert ContactInfo entity to ContactInfoResponse DTO
     */
    public ContactInfoResponse toResponse(ContactInfo contactInfo) {
        if (contactInfo == null) {
            return null;
        }

        return ContactInfoResponse.builder()
                .id(contactInfo.getId())
                .phoneNumber(contactInfo.getPhoneNumber())
                .mobileNumber(contactInfo.getMobileNumber())
                .emergencyContactName(contactInfo.getEmergencyContactName())
                .emergencyContactPhone(contactInfo.getEmergencyContactPhone())
                .address(toAddressInfo(contactInfo))
                .updatedAt(contactInfo.getUpdatedAt())
                .build();
    }

    /**
     * Convert ContactInfo fields to AddressInfo DTO
     */
    private ContactInfoResponse.AddressInfo toAddressInfo(ContactInfo contactInfo) {
        if (contactInfo == null) {
            return null;
        }

        return ContactInfoResponse.AddressInfo.builder()
                .line1(contactInfo.getAddressLine1())
                .line2(contactInfo.getAddressLine2())
                .city(contactInfo.getCity())
                .state(contactInfo.getState())
                .postalCode(contactInfo.getPostalCode())
                .country(contactInfo.getCountry())
                .build();
    }

    /**
     * Convert UpdateContactInfoRequest to ContactInfo entity
     */
    public ContactInfo toEntity(UpdateContactInfoRequest request) {
        if (request == null) {
            return null;
        }

        return ContactInfo.builder()
                .phoneNumber(request.getPhoneNumber())
                .mobileNumber(request.getMobileNumber())
                .emergencyContactName(request.getEmergencyContactName())
                .emergencyContactPhone(request.getEmergencyContactPhone())
                .addressLine1(request.getAddressLine1())
                .addressLine2(request.getAddressLine2())
                .city(request.getCity())
                .state(request.getState())
                .postalCode(request.getPostalCode())
                .country(request.getCountry())
                .build();
    }

    /**
     * Update ContactInfo entity from UpdateContactInfoRequest
     */
    public void updateEntityFromRequest(ContactInfo contactInfo,
                                        UpdateContactInfoRequest request) {
        if (contactInfo == null || request == null) {
            return;
        }

        contactInfo.setPhoneNumber(request.getPhoneNumber());
        contactInfo.setMobileNumber(request.getMobileNumber());
        contactInfo.setEmergencyContactName(request.getEmergencyContactName());
        contactInfo.setEmergencyContactPhone(request.getEmergencyContactPhone());
        contactInfo.setAddressLine1(request.getAddressLine1());
        contactInfo.setAddressLine2(request.getAddressLine2());
        contactInfo.setCity(request.getCity());
        contactInfo.setState(request.getState());
        contactInfo.setPostalCode(request.getPostalCode());
        contactInfo.setCountry(request.getCountry());
    }
}