import React, { useState, useEffect } from 'react';
import { PieChart, Pie, Cell, BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, LineChart, Line, ResponsiveContainer } from 'recharts';

const SpendingReportChart = () => {
    const [reportData, setReportData] = useState(null);
    const [loading, setLoading] = useState(true);

    // 더미 데이터
    const categoryData = [
        { name: '식비', value: 450000, color: '#FF6B6B' },
        { name: '교통비', value: 120000, color: '#4ECDC4' },
        { name: '쇼핑', value: 280000, color: '#45B7D1' },
        { name: '문화생활', value: 150000, color: '#96CEB4' },
        { name: '기타', value: 100000, color: '#FFEAA7' }
    ];

    const monthlyData = [
        { month: '1월', amount: 980000 },
        { month: '2월', amount: 1200000 },
        { month: '3월', amount: 1100000 },
        { month: '4월', amount: 950000 },
        { month: '5월', amount: 1300000 },
        { month: '6월', amount: 1100000 }
    ];

    const dailyTrend = [
        { date: '1주', spending: 250000 },
        { date: '2주', spending: 320000 },
        { date: '3주', spending: 280000 },
        { date: '4주', spending: 350000 }
    ];

    useEffect(() => {
        // 로딩 시뮬레이션
        setTimeout(() => {
            setReportData({
                totalSpending: 1100000,
                monthlyAverage: 1105000,
                categories: categoryData,
                aiInsight: "이번 달 식비 지출이 평소보다 20% 증가했습니다. 배달음식 주문이 늘어난 것이 주요 원인으로 보입니다."
            });
            setLoading(false);
        }, 1500);
    }, []);

    if (loading) {
        return (
            <div style={{
                padding: '20px',
                textAlign: 'center',
                minHeight: '100vh',
                backgroundColor: '#f8f9fa'
            }}>
                <div style={{
                    padding: '40px',
                    backgroundColor: 'white',
                    borderRadius: '12px',
                    boxShadow: '0 2px 10px rgba(0,0,0,0.1)'
                }}>
                    <h2 style={{ color: '#2c3e50', marginBottom: '20px' }}>지출 분석 리포트 로딩중...</h2>
                    <div style={{
                        width: '50px',
                        height: '50px',
                        border: '4px solid #f3f3f3',
                        borderTop: '4px solid #3498db',
                        borderRadius: '50%',
                        animation: 'spin 1s linear infinite',
                        margin: '20px auto'
                    }}></div>
                </div>
                <style>
                    {`
            @keyframes spin {
              0% { transform: rotate(0deg); }
              100% { transform: rotate(360deg); }
            }
          `}
                </style>
            </div>
        );
    }

    return (
        <div style={{
            padding: '20px',
            backgroundColor: '#f8f9fa',
            minHeight: '100vh',
            fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif'
        }}>
            {/* 헤더 */}
            <div style={{
                backgroundColor: '#2c3e50',
                color: 'white',
                padding: '20px',
                borderRadius: '12px',
                marginBottom: '20px',
                textAlign: 'center'
            }}>
                <h1 style={{ margin: '0 0 10px 0', fontSize: '24px' }}>💰 지출 분석 리포트</h1>
                <p style={{ margin: '0', opacity: '0.9' }}>2024년 6월 기준</p>
            </div>

            {/* 요약 카드 */}
            <div style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
                gap: '15px',
                marginBottom: '30px'
            }}>
                <div style={{
                    backgroundColor: 'white',
                    padding: '20px',
                    borderRadius: '12px',
                    boxShadow: '0 2px 10px rgba(0,0,0,0.1)',
                    textAlign: 'center'
                }}>
                    <h3 style={{ color: '#e74c3c', margin: '0 0 10px 0' }}>이번 달 총 지출</h3>
                    <p style={{ fontSize: '28px', fontWeight: 'bold', margin: '0', color: '#2c3e50' }}>
                        {reportData.totalSpending.toLocaleString()}원
                    </p>
                </div>
                <div style={{
                    backgroundColor: 'white',
                    padding: '20px',
                    borderRadius: '12px',
                    boxShadow: '0 2px 10px rgba(0,0,0,0.1)',
                    textAlign: 'center'
                }}>
                    <h3 style={{ color: '#3498db', margin: '0 0 10px 0' }}>월평균 지출</h3>
                    <p style={{ fontSize: '28px', fontWeight: 'bold', margin: '0', color: '#2c3e50' }}>
                        {reportData.monthlyAverage.toLocaleString()}원
                    </p>
                </div>
            </div>

            {/* AI 인사이트 */}
            <div style={{
                backgroundColor: 'white',
                padding: '20px',
                borderRadius: '12px',
                boxShadow: '0 2px 10px rgba(0,0,0,0.1)',
                marginBottom: '30px',
                border: '2px solid #f39c12'
            }}>
                <h3 style={{ color: '#f39c12', margin: '0 0 15px 0' }}>AI 분석 인사이트</h3>
                <p style={{
                    margin: '0',
                    fontSize: '16px',
                    lineHeight: '1.6',
                    color: '#2c3e50',
                    backgroundColor: '#fef9e7',
                    padding: '15px',
                    borderRadius: '8px'
                }}>
                    {reportData.aiInsight}
                </p>
            </div>

            {/* 카테고리별 지출 원형 차트 */}
            <div style={{
                backgroundColor: 'white',
                padding: '20px',
                borderRadius: '12px',
                boxShadow: '0 2px 10px rgba(0,0,0,0.1)',
                marginBottom: '30px'
            }}>
                <h3 style={{ color: '#2c3e50', margin: '0 0 20px 0' }}>카테고리별 지출 분석</h3>
                <ResponsiveContainer width="100%" height={300}>
                    <PieChart>
                        <Pie
                            data={categoryData}
                            cx="50%"
                            cy="50%"
                            outerRadius={100}
                            dataKey="value"
                            label={({ name, value }) => `${name}: ${(value / 1000).toFixed(0)}만원`}
                        >
                            {categoryData.map((entry, index) => (
                                <Cell key={`cell-${index}`} fill={entry.color} />
                            ))}
                        </Pie>
                        <Tooltip formatter={(value) => `${value.toLocaleString()}원`} />
                    </PieChart>
                </ResponsiveContainer>
            </div>

            {/* 월별 지출 트렌드 */}
            <div style={{
                backgroundColor: 'white',
                padding: '20px',
                borderRadius: '12px',
                boxShadow: '0 2px 10px rgba(0,0,0,0.1)',
                marginBottom: '30px'
            }}>
                <h3 style={{ color: '#2c3e50', margin: '0 0 20px 0' }}>월별 지출</h3>
                <ResponsiveContainer width="100%" height={300}>
                    <BarChart data={monthlyData}>
                        <CartesianGrid strokeDasharray="3 3" />
                        <XAxis dataKey="month" />
                        <YAxis tickFormatter={(value) => `${(value / 10000).toFixed(0)}만`} />
                        <Tooltip formatter={(value) => `${value.toLocaleString()}원`} />
                        <Bar dataKey="amount" fill="#3498db" radius={[4, 4, 0, 0]} />
                    </BarChart>
                </ResponsiveContainer>
            </div>

            {/* 주별 소비 패턴 */}
            <div style={{
                backgroundColor: 'white',
                padding: '20px',
                borderRadius: '12px',
                boxShadow: '0 2px 10px rgba(0,0,0,0.1)',
                marginBottom: '30px'
            }}>
                <h3 style={{ color: '#2c3e50', margin: '0 0 20px 0' }}>이번 달 주별 소비 패턴</h3>
                <ResponsiveContainer width="100%" height={250}>
                    <LineChart data={dailyTrend}>
                        <CartesianGrid strokeDasharray="3 3" />
                        <XAxis dataKey="date" />
                        <YAxis tickFormatter={(value) => `${(value / 10000).toFixed(0)}만`} />
                        <Tooltip formatter={(value) => `${value.toLocaleString()}원`} />
                        <Line
                            type="monotone"
                            dataKey="spending"
                            stroke="#e74c3c"
                            strokeWidth={3}
                            dot={{ r: 6 }}
                        />
                    </LineChart>
                </ResponsiveContainer>
            </div>


        </div>
    );
};

export default SpendingReportChart;