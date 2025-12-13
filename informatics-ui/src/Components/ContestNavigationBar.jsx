import { Box, Button, AppBar, Toolbar } from '@mui/material'
import { useParams, useLocation, NavLink } from 'react-router-dom'
import getMessage from './lang'

/**
 * ContestNavigationBar Component
 * 
 * Provides navigation links for contest-related pages:
 * - List: Links to contest task list or upsolving task list
 * - My Submissions: Links to user's submissions for the contest
 * - All Submissions: Links to all submissions for the contest
 * - Standings: Links to contest standings/results
 */
export default function ContestNavigationBar() {
    const { contest_id, problem_id } = useParams()
    const location = useLocation()
    
    // Determine if we're on a problem statement page or task list page
    const isProblemPage = !!problem_id
    const isUpsolving = location.pathname.includes('/archive') || location.pathname.includes('/upsolving')
    
    if (!contest_id) {
        return null
    }
    
    const contestBasePath = `/contest/${contest_id}`
    const listPath = isUpsolving ? `/archive` : contestBasePath
    const mySubmissionsPath = `${contestBasePath}/mySubmissions`
    const allSubmissionsPath = `${contestBasePath}/submissions`
    const standingsPath = `/results/${contest_id}`
    
    const linkStyle = {
        textDecoration: 'none',
        color: 'inherit',
        marginRight: '16px'
    }

    
    return (
        <AppBar 
            position="static" 
            sx={{ 
                backgroundColor: '#f5f5f5',
                boxShadow: '0 1px 3px rgba(0,0,0,0.1)',
                marginBottom: '20px'
            }}
        >
            <Toolbar sx={{ minHeight: '48px !important', padding: '0 16px' }}>
                <Box sx={{ display: 'flex', alignItems: 'center', width: '100%' }}>
                    {isProblemPage ? (
                        <Button
                            component={NavLink}
                            to={listPath}
                            sx={{
                                color: '#452c54',
                                textTransform: 'none',
                                fontSize: '14px',
                                '&:hover': {
                                    backgroundColor: 'rgba(69, 44, 84, 0.08)'
                                }
                            }}
                        >
                            სია
                        </Button>
                    ) : (
                        <Button
                            component={NavLink}
                            to={listPath}
                            sx={{
                                color: '#452c54',
                                textTransform: 'none',
                                fontSize: '14px',
                                fontWeight: 'bold',
                                '&:hover': {
                                    backgroundColor: 'rgba(69, 44, 84, 0.08)'
                                }
                            }}
                        >
                            სია
                        </Button>
                    )}
                    
                    {/* My Submissions link */}
                    <Button
                        component={NavLink}
                        to={mySubmissionsPath}
                        sx={{
                            color: '#452c54',
                            textTransform: 'none',
                            fontSize: '14px',
                            marginLeft: '8px',
                            '&:hover': {
                                backgroundColor: 'rgba(69, 44, 84, 0.08)'
                            },
                            '&.active': {
                                fontWeight: 'bold',
                                textDecoration: 'underline'
                            }
                        }}
                    >
                        ჩემი მცდელობები
                    </Button>
                    
                    {/* All Submissions link */}
                    <Button
                        component={NavLink}
                        to={allSubmissionsPath}
                        sx={{
                            color: '#452c54',
                            textTransform: 'none',
                            fontSize: '14px',
                            marginLeft: '8px',
                            '&:hover': {
                                backgroundColor: 'rgba(69, 44, 84, 0.08)'
                            },
                            '&.active': {
                                fontWeight: 'bold',
                                textDecoration: 'underline'
                            }
                        }}
                    >
                        ყველა მცდელობა
                    </Button>
                    
                    {/* Standings link */}
                    <Button
                        component={NavLink}
                        to={standingsPath}
                        sx={{
                            color: '#452c54',
                            textTransform: 'none',
                            fontSize: '14px',
                            marginLeft: '8px',
                            '&:hover': {
                                backgroundColor: 'rgba(69, 44, 84, 0.08)'
                            },
                            '&.active': {
                                fontWeight: 'bold',
                                textDecoration: 'underline'
                            }
                        }}
                    >
                        შედეგები
                    </Button>
                </Box>
            </Toolbar>
        </AppBar>
    )
}

