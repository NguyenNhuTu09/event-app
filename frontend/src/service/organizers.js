import { apiCall } from './api';

// ===== ORGANIZERS API =====
export const organizersAPI = {
    // Register as organizer (Requires authenticated user)
    registerOrganizer: async (organizerData) => {
        return apiCall('/organizers', {
            method: 'POST',
            body: JSON.stringify(organizerData),
        });
    },

    // Get organizer by ID (Requires authenticated user)
    getOrganizerById: async (organizerId) => {
        return apiCall(`/organizers/${organizerId}`, { method: 'GET' });
    },

    // Update organizer (Requires SADMIN or ORGANIZER role)
    updateOrganizer: async (organizerId, organizerData) => {
        return apiCall(`/organizers/${organizerId}`, {
            method: 'PUT',
            body: JSON.stringify(organizerData),
        });
    },

    // Super Admin APIs (Requires SADMIN role)
    getAllOrganizers: async () => {
        return apiCall('/organizers', { method: 'GET' });
    },

    approveOrganizer: async (organizerId) => {
        return apiCall(`/organizers/${organizerId}/approve`, { method: 'PUT' });
    },

    deleteOrganizer: async (organizerId) => {
        return apiCall(`/organizers/${organizerId}`, { method: 'DELETE' });
    },
};

// ===== USERS API =====
export const usersAPI = {
    // Get current user profile (Requires authenticated user)
    getCurrentUser: async () => {
        return apiCall('/users/me', { method: 'GET' });
    },

    updateCurrentUser: async (userData) => {
        return apiCall('/users/me', {
            method: 'PUT',
            body: JSON.stringify(userData),
        });
    },

    changePassword: async (changePasswordData) => {
        return apiCall('/users/me/change-password', {
            method: 'POST',
            body: JSON.stringify(changePasswordData),
        });
    },

    // Super Admin APIs (Requires SADMIN role)
    getAllUsers: async () => {
        return apiCall('/users', { method: 'GET' });
    },

    getUserById: async (userId) => {
        return apiCall(`/users/${userId}`, { method: 'GET' });
    },

    deleteUser: async (userId) => {
        return apiCall(`/users/${userId}`, { method: 'DELETE' });
    },

    searchUserByEmail: async (email) => {
        return apiCall(`/users/search?email=${encodeURIComponent(email)}`, { method: 'GET' });
    },
};
