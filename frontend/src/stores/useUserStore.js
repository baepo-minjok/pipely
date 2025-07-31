import {defineStore} from "pinia";
import {reactive, ref} from "vue";
import {userApi} from "@/api/UserApi.js";

export const useUserStore = defineStore(
    "userStore", () => {
        const isFetched = ref(false);
        const expiresAt = ref(null);

        const userInfo = reactive({
            name: "",
            email: "",
            isVerified: false,
            connected: false,
            infoList: [],
        });

        async function fetchUserInfo() {
            try {
                const response = await userApi.getUserDetail();

                if (response.status === 200) {
                    const data = response.data.data;

                    userInfo.name = data.name;
                    userInfo.email = data.email;
                    userInfo.isVerified = data.verified;
                    userInfo.infoList = data.infoDtoList;
                    userInfo.connected = data.connected;

                    expiresAt.value = Math.floor(Date.now() / 1000) + (30 * 60);

                    isFetched.value = true;
                } else {
                    isFetched.value = false;
                }
            } catch (error) {
                isFetched.value = false;
            }
        }

        function reset() {
            userInfo.name = "";
            userInfo.email = "";
            userInfo.isVerified = false;
            userInfo.infoList = [];
            isFetched.value = false;
            expiresAt.value = null;
        }

        function getUserInfo() {
            return userInfo;
        }

        return {
            userInfo,
            fetchUserInfo,
            isFetched,
            expiresAt,
            getUserInfo,
            reset,
        };
    },
    {
        persist: {
            enabled: true,
            strategies: [
                {
                    storage: sessionStorage,
                    paths: ["userInfo", "expiresAt"],
                },
            ],
        },
    },
);