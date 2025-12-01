// src/App.jsx
import { Routes, Route } from 'react-router-dom';
import SpendingReportChart from './SpendingReportChart';
import TermsAgreementPage from './TermsAgreementPage';

function App() {
  return (
    <div style={{
      width: '100vw',
      minHeight: '100vh',
      overflowX: 'hidden',
    }}>
      <Routes>
        {/* 지출 분석 리포트 */}
        <Route path="/report/spending" element={<SpendingReportChart />} />

        {/* 이용약관 동의 */}
        <Route path="/terms/agreement" element={<TermsAgreementPage />} />

        {/* 기본 경로는 리포트로 리다이렉트 */}
        <Route path="/" element={<SpendingReportChart />} />
      </Routes>
    </div>
  );
}

export default App;