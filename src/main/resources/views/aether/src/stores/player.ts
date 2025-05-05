import { defineStore } from 'pinia';
import { ref } from 'vue';
import PlayerApi, { type GetCurrentPlayerVo } from '@/commons/api/PlayerApi';

export const usePlayerStore = defineStore('player', () => {
    // State: Holds the currently active player information
    const currentPlayer = ref<GetCurrentPlayerVo | null>(null);
    const isLoading = ref(false);

    // Action: Fetches player info from the backend and updates the state
    async function updatePlayerInfo() {
        isLoading.value = true;
        try {
            const playerInfo = await PlayerApi.getCurrentPlayer();
            currentPlayer.value = playerInfo;
        } catch (error) {
            console.warn('Failed to fetch current player info:', error);
            currentPlayer.value = null; // Reset on error
        } finally {
            isLoading.value = false;
        }
    }

    // Action: Clears the current player info (e.g., on detach or logout)
    function clearPlayerInfo() {
        currentPlayer.value = null;
    }

    return {
        currentPlayer,
        isLoading,
        updatePlayerInfo,
        clearPlayerInfo
    };
});
