import {createContext, useCallback, useContext, useEffect, useState} from 'react'
import Cookies from 'js-cookie';
import {AxiosContext} from '../utils/axiosInstance'
import {renewCsrfToken} from "../utils/csrfUtils";


export const AuthContext = createContext({
    isLoggedIn: false,
    username: '',
    role: '',
    login: () => {
    },
    logout: () => {
    },
})

export const AuthContextProvider = (props) => {
    const axiosInstance = useContext(AxiosContext)
    const [isLoggedIn, setIsLoggedIn] = useState(false)
    const [role, setRole] = useState('')
    const [username, setUsername] = useState('')
    const [authLoading, setAuthLoading] = useState(true)
    useEffect(() => {
        const fetchUsername = async () => {
            setAuthLoading(true)
            axiosInstance.get('/user').then((response) => {
                setUsername(response.data.username);
                setRole(response.data.role);
                setIsLoggedIn(true);
                setAuthLoading(false);
            }).catch((error) => {
                setIsLoggedIn(false);
                setUsername('');
                setRole('');
                setAuthLoading(false);
            })
        };

        fetchUsername();
    }, []);

    const logoutHandler = useCallback(() => {
        setIsLoggedIn(false)
        setUsername('')
        setRole('')
        renewCsrfToken()
    }, [])

    const loginHandler = useCallback(({username, role}) => {
        setIsLoggedIn(true)
        setRole(role)
        setUsername(username)
    }, [])

    const contextValue = {
        isLoggedIn: isLoggedIn,
        username: isLoggedIn ? username : '',
        role: isLoggedIn ? role : '',
        authLoading: authLoading,
        login: loginHandler,
        logout: logoutHandler,
    }

    return (
        <AuthContext.Provider value={contextValue}>
            {props.children}
        </AuthContext.Provider>
    )
}
