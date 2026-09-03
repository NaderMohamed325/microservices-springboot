package com.neo.customerservice.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserRolesTest {

    @Test
    void getAuthority_admin_returnsRoleAdmin() {
        assertThat(UserRoles.ADMIN.getAuthority()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    void getAuthority_customer_returnsRoleCustomer() {
        assertThat(UserRoles.CUSTOMER.getAuthority()).isEqualTo("ROLE_CUSTOMER");
    }

    @Test
    void values_containsAllRoles() {
        assertThat(UserRoles.values()).hasSize(2);
        assertThat(UserRoles.valueOf("ADMIN")).isEqualTo(UserRoles.ADMIN);
        assertThat(UserRoles.valueOf("CUSTOMER")).isEqualTo(UserRoles.CUSTOMER);
    }
}
