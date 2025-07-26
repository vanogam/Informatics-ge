import React, { useState, useContext } from 'react';
import { AxiosContext } from '../../utils/axiosInstance';
import { Box, Typography, Button } from '@mui/material';

export default function Comment({ comment }) {
    const axiosInstance = useContext(AxiosContext);
    const [childComments, setChildComments] = useState([]);
    const [showReplies, setShowReplies] = useState(false);

    const loadChildComments = () => {
        if (childComments.length > 0) {
            return;
        }
        axiosInstance.get(`/posts/${comment.postId}/comment/${comment.id}`)
            .then((response) => {
                setChildComments(response.data);
                setShowReplies(true);
            })
            .catch((error) => {
                console.error('Error loading child comments:', error);
            });
    };

    return (
        <Box sx={{ marginBottom: '20px', paddingLeft: '20px', borderLeft: '1px solid #ccc' }}>
            <Typography variant="subtitle2" fontWeight="bold">
                {comment.username}
            </Typography>
            <Typography variant="body1" sx={{ marginTop: '5px' }}>
                {comment.text}
            </Typography>
            {comment.childCount > 0 && !showReplies && (
                <Button
                    variant="text"
                    color="primary"
                    sx={{ marginTop: '10px' }}
                    onClick={loadChildComments}
                >
                    {`(${comment.childCount} replies)`}
                </Button>
            )}
            {showReplies && (
                <Box sx={{ marginTop: '10px' }}>
                    {childComments.map((childComment) => (
                        <Comment key={childComment.id} comment={childComment} />
                    ))}
                </Box>
            )}
        </Box>
    );
}