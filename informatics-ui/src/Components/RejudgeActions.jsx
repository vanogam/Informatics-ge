import {useContext} from 'react'
import Button from '@mui/material/Button'
import Stack from '@mui/material/Stack'
import Tooltip from '@mui/material/Tooltip'
import {toast} from 'react-toastify'
import {AxiosContext} from '../utils/axiosInstance'
import {AuthContext} from '../store/authentication'
import {useConfirmDialog} from '../utils/ConfirmDialogContext'
import getMessage from './lang'

/**
 * The actions offered on every row, in the order they nest: recompiling ends by running, running
 * ends by scoring. The single-letter labels keep the column narrow enough to sit beside the status
 * without crowding it.
 *
 * `sourceOnly` marks the ones that need a program. An output submission has none - and re-running
 * it would re-judge tests the contestant uploaded no answer for - so it is offered only the
 * rescore, matching the server, which refuses the other two outright.
 */
const REJUDGE_ACTIONS = [
    {action: 'RECOMPILE', label: 'C', tooltip: 'rejudgeRecompile', sourceOnly: true},
    {action: 'RERUN', label: 'R', tooltip: 'rejudgeRerun', sourceOnly: true},
    {action: 'RESCORE', label: 'S', tooltip: 'rejudgeRescore'},
]

/**
 * Whether the current user may re-judge. Exported so a list can decide whether to make room for
 * the column at all - the buttons themselves render nothing for everyone else.
 */
export function useCanRejudge() {
    const authContext = useContext(AuthContext)
    // Roles arrive as a comma separated string, so a substring test rather than an equality one.
    return !!authContext.role && authContext.role.includes('ADMIN')
}

/**
 * The re-judge buttons for one submission: rebuild it, re-run it, or only recompute its score.
 *
 * @param submission the row's submission; only its id is sent.
 * @param onDone called after the server has answered, so the list can show the new status without
 *               waiting out its poll - or at all, on the lists that do not poll.
 */
export default function RejudgeActions({submission, onDone = () => {}}) {
    const axiosInstance = useContext(AxiosContext)
    const {showConfirmDialog} = useConfirmDialog()
    const canRejudge = useCanRejudge()

    if (!canRejudge) {
        return null
    }

    /**
     * Refusals come back as 200 with a reason per submission, so they are reported here; genuine
     * failures are already toasted once by the axios interceptor and must not be toasted twice.
     */
    const runRejudge = (action) => {
        axiosInstance.post('/admin/submissions/rejudge', {action, submissionIds: [submission.id]})
            .then((response) => {
                const results = response?.data?.results || []
                const refused = results.filter((result) => result.outcome === 'REFUSED')
                if (refused.length > 0) {
                    // The server's reason codes double as dictionary keys.
                    refused.forEach((result) => toast.error(getMessage('ka', result.code)))
                } else {
                    toast.success(getMessage('ka', action === 'RESCORE' ? 'rescoreDone' : 'rejudgeQueued'))
                }
                onDone()
            })
            .catch(() => {})
    }

    const confirmRejudge = (action) => {
        showConfirmDialog({
            type: 'warning',
            title: getMessage('ka', 'rejudgeConfirmTitle'),
            message: getMessage('ka', `confirmRejudge_${action}`, submission.id),
            onConfirm: () => runRejudge(action),
        })
    }

    const actions = submission.kind === 'OUTPUT'
        ? REJUDGE_ACTIONS.filter(({sourceOnly}) => !sourceOnly)
        : REJUDGE_ACTIONS

    return (
        <Stack direction="row" spacing={0.5} justifyContent="flex-end">
            {actions.map(({action, label, tooltip}) => (
                <Tooltip key={action} title={getMessage('ka', tooltip)}>
                    <Button
                        size="small"
                        variant="outlined"
                        sx={{minWidth: '2rem', padding: '0 0.25rem'}}
                        // The whole row opens the submission, so the click stops here.
                        onClick={(event) => {
                            event.stopPropagation()
                            confirmRejudge(action)
                        }}
                    >
                        {label}
                    </Button>
                </Tooltip>
            ))}
        </Stack>
    )
}
