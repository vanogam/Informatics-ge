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
 * - Standings: Links to contest standings/results (contest pages only - the archive spans many
 *   contests, so there's no single standings page for it)
 */
export default function ContestNavigationBar() {
    const { contest_id, problem_id } = useParams()
    const location = useLocation()

    // Determine if we're on a problem statement page or task list page
    const isProblemPage = !!problem_id
    const isArchivePage = location.pathname.startsWith('/archive')
    const isUpsolving = isArchivePage || location.pathname.includes('/upsolving')

    if (!contest_id && !isArchivePage) {
        return null
    }

    const contestBasePath = contest_id ? `/contest/${contest_id}` : null
    const listPath = isUpsolving ? `/archive` : contestBasePath
    const mySubmissionsPath = isUpsolving ? `/archive/mySubmissions` : `${contestBasePath}/mySubmissions`
    const allSubmissionsPath = isUpsolving ? `/archive/status` : `${contestBasePath}/submissions`
    const standingsPath = contest_id ? `/results/${contest_id}` : null
    
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
                    
                    {/* Standings link - not shown for the archive, which spans many contests */}
                    {standingsPath && (
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
                    )}
                </Box>
            </Toolbar>
        </AppBar>
    )
}

