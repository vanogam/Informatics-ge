import React, { useEffect, useState, useContext } from 'react';
import { AxiosContext } from '../../utils/axiosInstance';
import Comment from './Comment';
import { Button, Box, Typography } from '@mui/material';

function CommentsSection({ postId, totalCount }) {
    const axiosInstance = useContext(AxiosContext);
    const [comments, setComments] = useState([]);
    const [pageNum, setPageNum] = useState(0);
    const pageSize = 10;
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        setComments([]);
        setPageNum(0);
        loadComments(0);
    }, [postId]);

    const loadComments = (page) => {
        setLoading(true);
        axiosInstance.get(`/posts/${postId}/comments?pageNum=${page}&pageSize=${pageSize}`)
            .then((response) => {
                setComments(prev => [...prev, ...response.data]);
                setLoading(false);
            })
            .catch(() => setLoading(false));
    };

    const handleShowMore = () => {
        const nextPage = pageNum + 1;
        setPageNum(nextPage);
        loadComments(nextPage);
    };

    return (
        <Box>
            {comments.map(comment => (
                <Comment key={comment.id} comment={comment} />
            ))}
            {comments.length < totalCount && (
                <Button
                    variant="outlined"
                    onClick={handleShowMore}
                    disabled={loading}
                    sx={{ marginTop: '10px' }}
                >
                    {loading ? 'Loading...' : 'Show more comments'}
                </Button>
            )}
            {comments.length === 0 && !loading && (
                <Typography color="textSecondary">No comments yet.</Typography>
            )}
        </Box>
    );
}

export { CommentsSection };