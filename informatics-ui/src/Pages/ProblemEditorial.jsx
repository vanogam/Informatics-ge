import {useParams} from 'react-router-dom'
import {Box, Typography} from '@mui/material'
import React, {useContext, useEffect, useState} from 'react'
import {AxiosContext} from '../utils/axiosInstance'
import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";
import "../styles/markdown.css";
import remarkMath from "remark-math";
import rehypeMathjax from "rehype-mathjax";
import getMessage from "../Components/lang";
import ContestNavigationBar from "../Components/ContestNavigationBar";
import markdownComponents from "../utils/markdownComponents";

/**
 * Read-only editorial view. The tab that links here is already greyed out when there is nothing
 * to show, but a direct URL visit (or a stale tab) can still land here with no content - in that
 * case this just says so rather than rendering an empty page.
 */
export default function ProblemEditorial() {
    const {problem_id} = useParams()
    const axiosInstance = useContext(AxiosContext);
    const [taskOrder, setTaskOrder] = useState(null)
    const [taskTitle, setTaskTitle] = useState('')
    const [editorial, setEditorial] = useState(null)

    useEffect(() => {
        axiosInstance.get(`/task/${problem_id}`)
            .then((response) => {
                setTaskOrder(response.data.order)
                setTaskTitle(response.data.title)
            })
            .catch(_ => {})

        axiosInstance.get(`/task/${problem_id}/editorial/KA`)
            .then((response) => {
                setEditorial(response.data.editorial || '')
            })
            .catch(_ => {})
    }, [problem_id])

    const heading = [taskOrder ? `${taskOrder}.` : '', taskTitle].filter(Boolean).join(' ').trim();

    return (
        <Box>
            <ContestNavigationBar/>
            <Box sx={{marginLeft: '10%', marginTop: '5%', width: '60%'}}>
                {heading && (
                    <Typography variant='h6' sx={{marginBottom: '1rem'}}>
                        {heading} - {getMessage('ka', 'editorial')}
                    </Typography>
                )}
                {editorial ? (
                    <div className="markdown-body">
                        <ReactMarkdown
                            children={editorial}
                            remarkPlugins={[remarkMath, remarkGfm]}
                            rehypePlugins={[rehypeMathjax]}
                            components={markdownComponents}
                            urlTransform={url => `/api/task/${problem_id}/image/${url}`}
                        />
                    </div>
                ) : (
                    editorial !== null && (
                        <Typography color='textSecondary'>{getMessage('ka', 'noEditorialAvailable')}</Typography>
                    )
                )}
            </Box>
        </Box>
    );
}
