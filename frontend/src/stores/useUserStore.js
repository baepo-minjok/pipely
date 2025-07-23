import {defineStore} from "pinia";
import {reactive, ref} from "vue";
import {userApi} from "@/api/UserApi.js";

export const useUserStore = defineStore(
    "userStore", () => {

        const isFetched = ref(false);

        const userInfo = reactive({
            name: "",
            email: "",
            isVerified: false,
            infoList: [],
        });

        async function fetchUserInfo() {
            const response = await userApi.getUserDetail();

            if (response.status === 200) {
                const data = response.data.data;

                userInfo.name = data.name;
                userInfo.email = data.email;
                userInfo.isVerified = data.verified;
                userInfo.infoList = data.infoDtoList;

                isFetched.value = true;
            } else {

                isFetched.value = false;
            }
        }

        function reset() {
            userInfo.name = "";
            userInfo.email = "";
            userInfo.isVerified = false;
            userInfo.infoList = [];
            isFetched.value = false;
        }

        function getUserInfo() {
            return userInfo;
        }

        return {
            userInfo,
            fetchUserInfo,
            isFetched,
            getUserInfo,
            reset
        };
    },
    {
        persist: {
            enabled: true,
            strategies: [
                {
                    storage: sessionStorage,
                    paths: ["userInfo"],
                },

            ],
        },
    },
)