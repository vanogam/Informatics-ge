import {Box, Button, AppBar, Toolbar} from '@mui/material'
import {NavLink} from 'react-router-dom'
import getMessage from './lang'

/**
 * Top menu for the administration section, mirroring ContestNavigationBar's look: workers is the
 * default/first tab, then all submissions, then users (a stub for now).
 */
export default function AdminNavigationBar() {
    const linkSx = {
        color: '#452c54',
        textTransform: 'none',
        fontSize: '14px',
        marginLeft: '8px',
        '&:hover': {backgroundColor: 'rgba(69, 44, 84, 0.08)'},
        '&.active': {fontWeight: 'bold', textDecoration: 'underline'},
    }

    return (
        <AppBar
            position="static"
            sx={{
                backgroundColor: '#f5f5f5',
                boxShadow: '0 1px 3px rgba(0,0,0,0.1)',
                marginBottom: '20px',
            }}
        >
            <Toolbar sx={{minHeight: '48px !important', padding: '0 16px'}}>
                <Box sx={{display: 'flex', alignItems: 'center', width: '100%'}}>
                    <Button component={NavLink} to="/admin" end sx={{...linkSx, marginLeft: 0}}>
                        {getMessage('ka', 'adminWorkers')}
                    </Button>
                    <Button component={NavLink} to="/admin/submissions" sx={linkSx}>
                        {getMessage('ka', 'adminSubmissions')}
                    </Button>
                    <Button component={NavLink} to="/admin/users" sx={linkSx}>
                        {getMessage('ka', 'adminUsers')}
                    </Button>
                </Box>
            </Toolbar>
        </AppBar>
    )
}
