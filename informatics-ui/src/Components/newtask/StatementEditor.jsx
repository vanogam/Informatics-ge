import getMessage from "../lang";
import {Button, Stack} from "@mui/material";
import {useContext, useState} from "react";
import MarkdownEditor from "../markdownEditor";

const StatementEditor = ({taskId, statement, setStatement, saveStatement, loadStatement}) => {
    const [showStatementEditor, setShowStatementEditor] = useState(false);

    return <Stack gap='1rem' width='100%' mx='auto' mb='1rem'>
        <Button
            fullWidth
            variant='contained'
            component='label'
            onClick={() => setShowStatementEditor((prev) => !prev)}
        >
            {getMessage("ka", 'statement')}
        </Button>
        {showStatementEditor && (
            <div style={{marginTop: '1rem'}}>
                <MarkdownEditor
                    value={statement}
                    onChange={setStatement}
                    entries={[
                        {
                            align: 'center',
                            labelVisible: false,
                            label: getMessage('ka', 'title'),
                            value: statement.title,
                            onChange: (value) => setStatement({...statement, title: value}),
                            height: "2rem",
                        },
                        {
                            labelVisible: false,
                            label: getMessage('ka', 'statement'),
                            value: statement.statement,
                            onChange: (value) => setStatement({...statement, statement: value}),
                            height: "20rem",
                        },
                        {
                            labelVisible: true,
                            label: getMessage('ka', 'inputContent'),
                            value: statement.inputInfo,
                            onChange: (value) => setStatement({...statement, inputInfo: value}),
                            height: "2rem",
                        },
                        {
                            labelVisible: true,
                            label: getMessage('ka', 'outputContent'),
                            value: statement.outputInfo,
                            onChange: (value) => setStatement({...statement, outputInfo: value}),
                            height: "2rem",
                        },
                    ]}
                    imageUploadAddress={`/task/${taskId}/image`}
                    imageDownloadFunc={url => `/api/task/${taskId}/image/${url}`}
                    submitFunc={saveStatement}
                />
            </div>
        )}
    </Stack>;
}

export default StatementEditor;