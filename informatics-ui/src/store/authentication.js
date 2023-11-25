import { createContext, useCallback, useEffect, useState } from 'react'
import Cookies from 'js-cookie';
export const AuthContext = createContext({
	isLoggedIn: false,
	username: '',
	roles: [],
	login: () => {},
	logout: () => {},
})

export const AuthContextProvider = (props) => {
	const [isLoggedIn, setIsLoggedIn] = useState(false)
	const [username, setUsername] = useState('')
	useEffect(() => {
		if (Cookies.get('username')) {
			setIsLoggedIn(true)
			setUsername(Cookies.get('username'))
		} else {
			setIsLoggedIn(false)
			setUsername('')
		}
	}, [])

	const logoutHandler = useCallback(() => {
		Cookies.remove('username')
		Cookies.remove('roles')
		setIsLoggedIn(false)
		setUsername('')
	}, [])

	const loginHandler = useCallback(({ username, roles }) => {
		Cookies.set('username', username)
		Cookies.set('roles', roles)
		setIsLoggedIn(true)
		setUsername(Cookies.get('username'))
	}, [])

	const contextValue = {
		isLoggedIn: isLoggedIn,
		username: isLoggedIn ? Cookies.get('username') : '',
		roles: isLoggedIn ? Cookies.get('roles') : '',
		login: loginHandler,
		logout: logoutHandler,
	}

	return (
		<AuthContext.Provider value={contextValue}>
			{props.children}
		</AuthContext.Provider>
	)
}
