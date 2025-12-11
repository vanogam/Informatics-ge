import axios from 'axios'
import {toast} from 'react-toastify'
import getMessage from '../Components/lang'
import {AuthContext} from '../store/authentication'
import {createContext, useContext} from 'react'
import Cookies from 'js-cookie';

export const AxiosContext = createContext(null)

export const AxiosInstanceProvider = (props) => {
    const authContext = useContext(AuthContext)

    const getAxiosInstance = () => {

        const axiosInstance = axios.create({
                baseURL: `${process.env.REACT_APP_HOST}`,
                withCredentials: true,
                headers: {
                    'Access-Control-Allow-Origin': `*`,
                    'Access-Control-Allow-Headers': 'Origin, X-Requested-With, Content-Type, Accept',
                    'X-CSRF-Token': Cookies.get('XSRF-TOKEN') || '',
                },
            },
        );

        axiosInstance.interceptors.response.use((response) => {
            return response
        }, (error) => {
            const config = error.config || {};
            
            // Handle /user endpoint errors silently (used for auth checks)
            if (error.config?.url === '/user') {
                return Promise.reject(error);
            }
            
            // Check if error has a response (network errors won't have one)
            if (!error.response) {
                if (!config.ignoreErrors) {
                    toast.error(getMessage('ka', 'networkError') || 'Network error occurred');
                }
                return Promise.reject(error);
            }
            
            if (!config.ignoreErrors) {
                const status = error.response.status;
                switch (status) {
                    case 500:
                        toast.error(getMessage('ka', 'unexpectedException'));
                        break;
                    case 403:
                        toast.error(getMessage('ka', 'insufficientPrivileges'));
                        break;
                    case 401:
                        // Handle 401 globally - logout user and show message
                        authContext.logout();
                        toast.error(getMessage('ka', 'pleaseLogin'), {toastId: 'pleaseLogin'});
                        // Return a resolved promise to prevent error propagation
                        // This prevents unhandled errors from showing up client-side
                        // Code can check response.data === null and response.status === 401 if needed
                        return Promise.resolve({ 
                            data: null, 
                            status: 401, 
                            statusText: 'Unauthorized',
                            headers: error.response.headers,
                            config: error.config,
                            handled: true 
                        });
                    case 400:
                        toast.error(getMessage('ka', error.response.data.message))
                        break;
                }
            }
            return Promise.reject(error);
        })

        return axiosInstance
    }

    return (
        <AxiosContext.Provider value={getAxiosInstance()}>
            {props.children}
        </AxiosContext.Provider>
    )
}