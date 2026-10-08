import getMessage from "../lang";
import {Button, FormControlLabel, Stack, Switch} from "@mui/material";
import {useContext, useState} from "react";
import MarkdownEditor from "../markdownEditor";
import {AxiosContext} from "../../utils/axiosInstance";
import {toast} from "react-toastify";

/**
 * Mirrors StatementEditor exactly - same collapsible shell, same MarkdownEditor - but adds the
 * visibility checkbox a teacher uses to decide whether the editorial should be shown at all once
 * the task reaches upsolving (it is always hidden during a live contest, checkbox or not).
 */
const EditorialEditor = ({taskId, editorial, setEditorial, saveEditorial, visible, setVisible}) => {
    const [showEditorialEditor, setShowEditorialEditor] = useState(false);
    const axiosInstance = useContext(AxiosContext);

    const toggleVisible = (checked) => {
        axiosInstance.put(`/task/${taskId}/editorial/visible`, {visible: checked})
            .then(response => {
                if (response.status === 200) {
                    setVisible(checked);
                    toast.success(getMessage('ka', 'saved'));
                }
            });
    };

    return <Stack gap='1rem' width='100%' mx='auto' mb='1rem'>
        <Button
            fullWidth
            variant='contained'
            component='label'
            onClick={() => setShowEditorialEditor((prev) => !prev)}
        >
            {getMessage("ka", 'editorial')}
        </Button>
        {showEditorialEditor && (
            <div style={{marginTop: '1rem'}}>
                <FormControlLabel
                    control={<Switch checked={!!visible} onChange={(e) => toggleVisible(e.target.checked)}/>}
                    label={getMessage('ka', 'showEditorial')}
                />
                <MarkdownEditor
                    value={editorial}
                    onChange={setEditorial}
                    entries={[
                        {
                            labelVisible: false,
                            label: getMessage('ka', 'editorial'),
                            value: editorial,
                            onChange: setEditorial,
                            height: "20rem",
                        },
                    ]}
                    imageUploadAddress={`/task/${taskId}/image`}
                    imageDownloadFunc={url => `/api/task/${taskId}/image/${url}`}
                    submitFunc={saveEditorial}
                />
            </div>
        )}
    </Stack>;
}

export default EditorialEditor;
