import {Box, CircularProgress, Container, Typography} from '@mui/material'
import AdminNavigationBar from '../../Components/AdminNavigationBar'
import {useRequireAdmin} from '../../utils/useRequireAdmin'
import getMessage from '../../Components/lang'

/** TODO: user management (list, roles, search) - out of scope for now. */
export default function AdminUsers() {
    const {ready} = useRequireAdmin()

    if (!ready) {
        return (
            <>
                <AdminNavigationBar />
                <Box display="flex" justifyContent="center" alignItems="center" minHeight="400px">
                    <CircularProgress />
                </Box>
            </>
        )
    }

    return (
        <main>
            <AdminNavigationBar />
            <Container maxWidth="lg">
                <Typography align="center" color="text.secondary" mt="2rem">
                    {getMessage('ka', 'adminUsersComingSoon')}
                </Typography>
            </Container>
        </main>
    )
}
