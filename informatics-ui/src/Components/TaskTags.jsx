import {useContext, useState} from 'react'
import {Autocomplete, Chip, Stack, TextField} from '@mui/material'
import {toast} from 'react-toastify'
import {AxiosContext} from '../utils/axiosInstance'
import getMessage from './lang'

/**
 * Tags for one task, shown as chips below its title - used on both the task list and the
 * statement page. Staff (editable=true) gets an inline control to add an existing or brand new
 * tag, and an "x" on each chip to remove one; everyone else just sees the chips. Visibility
 * (upsolving-only, never during a live contest) and the write permission are both enforced
 * server-side - this component only renders whatever tags/editable it's handed.
 */
export default function TaskTags({taskId, tags, setTags, editable, allTags}) {
    const axiosInstance = useContext(AxiosContext)
    const [inputValue, setInputValue] = useState('')

    if (!editable && (!tags || tags.length === 0)) {
        return null
    }

    const addTag = (value) => {
        const tag = (value || '').trim()
        if (!tag || tags.includes(tag)) {
            return
        }
        axiosInstance.post(`/task/${taskId}/tags`, {tag})
            .then((response) => {
                if (response.status === 200) {
                    setTags([...tags, tag])
                    setInputValue('')
                }
            })
            .catch(() => toast.error(getMessage('ka', 'unexpectedException')))
    }

    const removeTag = (tag) => {
        axiosInstance.delete(`/task/${taskId}/tags/${encodeURIComponent(tag)}`)
            .then((response) => {
                if (response.status === 200) {
                    setTags(tags.filter((t) => t !== tag))
                }
            })
            .catch(() => toast.error(getMessage('ka', 'unexpectedException')))
    }

    return (
        <Stack direction="row" spacing={0.5} alignItems="center" flexWrap="wrap" justifyContent="flex-end"
               sx={{marginLeft: 'auto'}}
               onClick={(e) => editable && e.stopPropagation()}>
            {tags.map((tag) => (
                <Chip
                    key={tag}
                    label={tag}
                    size="small"
                    onDelete={editable ? () => removeTag(tag) : undefined}
                    sx={{
                        backgroundColor: '#616161',
                        color: '#fff',
                        '& .MuiChip-deleteIcon': {color: 'rgba(255, 255, 255, 0.7)'},
                        '& .MuiChip-deleteIcon:hover': {color: '#fff'},
                    }}
                />
            ))}
            {editable && (
                <Autocomplete
                    freeSolo
                    forcePopupIcon
                    openOnFocus
                    size="small"
                    options={(allTags || []).filter((t) => !tags.includes(t))}
                    inputValue={inputValue}
                    onInputChange={(_, value) => setInputValue(value)}
                    onChange={(_, value) => value && addTag(value)}
                    renderInput={(params) => (
                        <TextField {...params} placeholder={getMessage('ka', 'addTag')} variant="standard"/>
                    )}
                    sx={{minWidth: '9rem', display: 'inline-block'}}
                />
            )}
        </Stack>
    )
}
