import {useContext, useEffect} from 'react'
import {useNavigate} from 'react-router-dom'
import {AuthContext} from '../store/authentication'

/**
 * Bounces a non-admin visitor away from an admin page, once auth has finished loading. Shared by
 * every /admin/* page so they all enforce the same check the same way.
 */
export function useRequireAdmin() {
    const authContext = useContext(AuthContext)
    const navigate = useNavigate()

    useEffect(() => {
        if (authContext.authLoading) {
            return
        }
        if (!authContext.isLoggedIn || !authContext.role || !authContext.role.includes('ADMIN')) {
            navigate('/')
        }
    }, [authContext.isLoggedIn, authContext.role, authContext.authLoading, navigate])

    return {ready: !authContext.authLoading && authContext.isLoggedIn && authContext.role?.includes('ADMIN')}
}
