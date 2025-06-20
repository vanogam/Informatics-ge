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
    useEffect(() => {
        const fetchUsername = async () => {
            axiosInstance.get('/user').then((response) => {
                setUsername(response.data.username);
                setRole(response.data.role);
                setIsLoggedIn(true);
            }).catch((error) => {
                setIsLoggedIn(false);
                setUsername('');
                setRole('');
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
        login: loginHandler,
        logout: logoutHandler,
    }

    return (
        <AuthContext.Provider value={contextValue}>
            {props.children}
        </AuthContext.Provider>
    )
}
