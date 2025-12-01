import React, { useState, useEffect } from 'react';

const TermsAgreementPage = () => {
    const [expandedSections, setExpandedSections] = useState({});
    const [agreementStatus, setAgreementStatus] = useState({
        service: false,
        privacy: false,
        finance: false,
        marketing: false,
        location: false
    });
    const [allMandatoryAgreed, setAllMandatoryAgreed] = useState(false);

    const termsData = [
        {
            id: 'service',
            title: '서비스 이용약관',
            required: true,
            version: 'v2.1',
            date: '2024.06.01',
            content: `제1조 (목적)
본 약관은 OO은행(이하 "회사")이 제공하는 모바일 뱅킹 서비스(이하 "서비스")의 이용조건 및 절차, 회사와 이용자 간의 권리와 의무, 기타 필요한 사항을 규정함을 목적으로 합니다.

제2조 (용어의 정의)
1. "서비스"라 함은 회사가 제공하는 모든 모바일 뱅킹 서비스를 의미합니다.
2. "이용자"라 함은 본 약관에 따라 서비스를 이용하는 고객을 말합니다.
3. "계정"이라 함은 서비스 이용을 위해 이용자가 등록한 계정을 말합니다.

제3조 (서비스의 제공)
회사는 다음과 같은 서비스를 제공합니다:
• 계좌조회 및 거래내역 확인
• 계좌이체 및 송금서비스  
• 금융상품 조회 및 가입
• 대출 및 카드 서비스
• 기타 회사가 정하는 부가서비스

제4조 (서비스 이용시간)
서비스는 연중무휴 1일 24시간 제공함을 원칙으로 합니다. 다만, 시스템 점검 등 불가피한 사유로 서비스가 중단될 수 있습니다.`
        },
        {
            id: 'privacy',
            title: '개인정보 처리방침',
            required: true,
            version: 'v3.0',
            date: '2024.05.15',
            content: `제1조 (개인정보의 처리목적)
OO은행은 다음의 목적을 위하여 개인정보를 처리합니다:
• 서비스 제공에 관한 계약 이행 및 서비스 제공에 따른 요금정산
• 회원 관리: 회원제 서비스 이용에 따른 본인확인, 개인식별
• 마케팅 및 광고에의 활용: 이벤트 등 광고성 정보 전달

제2조 (개인정보의 처리 및 보유기간)
① 회사는 법령에 따른 개인정보 보유·이용기간 또는 정보주체로부터 개인정보를 수집시에 동의받은 개인정보 보유·이용기간 내에서 개인정보를 처리·보유합니다.
② 각각의 개인정보 처리 및 보유 기간은 다음과 같습니다:
• 계약 또는 청약철회 등에 관한 기록: 5년
• 대금결제 및 재화 등의 공급에 관한 기록: 5년
• 소비자의 불만 또는 분쟁처리에 관한 기록: 3년

제3조 (개인정보 제3자 제공)
회사는 정보주체의 동의, 법률의 특별한 규정 등 개인정보보호법 제17조 및 제18조에 해당하는 경우에만 개인정보를 제3자에게 제공합니다.`
        },
        {
            id: 'finance',
            title: '전자금융거래 이용약관',
            required: true,
            version: 'v1.8',
            date: '2024.04.30',
            content: `제1조 (목적)
본 약관은 OO은행과 전자금융거래 서비스를 이용하는 고객간의 전자금융거래에 관한 기본사항을 정함으로써 거래의 신속하고 효율적인 처리를 도모하고 거래당사자 상호간의 이해관계를 합리적으로 조정하는 것을 목적으로 합니다.

제2조 (전자금융거래의 종류)
본 약관에서 정하는 전자금융거래는 다음과 같습니다:
• 인터넷뱅킹을 이용한 거래
• 모바일뱅킹을 이용한 거래
• 폰뱅킹을 이용한 거래
• 기타 전자적 장치를 통한 금융거래

제3조 (접근매체의 관리)
① 고객은 접근매체를 제3자에게 대여하거나 사용을 위임하거나 양도 또는 담보목적으로 제공하여서는 안됩니다.
② 고객은 자신의 접근매체를 안전하게 관리해야 하며, 접근매체의 위조나 변조를 방지하기 위한 충분한 주의를 기울여야 합니다.

제4조 (거래내용의 확인)
① 은행은 전자금융거래에 관하여 확인을 요청받은 때에는 지체없이 그 결과를 알려드립니다.
② 고객이 제1항의 확인을 요청한 경우 거래내용에 오류가 있음을 알았을 때에는 즉시 그 정정을 요구할 수 있습니다.`
        },
        {
            id: 'marketing',
            title: '마케팅 정보 수신 동의',
            required: false,
            version: 'v1.2',
            date: '2024.06.01',
            content: `제1조 (마케팅 정보 제공 목적)
회사는 고객에게 다음과 같은 마케팅 정보를 제공하고자 합니다:
• 신상품 및 서비스 안내
• 이벤트 및 프로모션 정보
• 맞춤형 금융상품 추천
• 회사 소식 및 공지사항

제2조 (정보 제공 방법)
마케팅 정보는 다음의 방법으로 제공됩니다:
• 모바일 앱 푸시 알림
• SMS, LMS, MMS
• 이메일
• 우편물
• 전화

제3조 (동의철회)
① 고객은 언제든지 마케팅 정보 수신에 대한 동의를 철회할 수 있습니다.
② 동의철회는 고객센터, 모바일 앱, 홈페이지를 통해 가능합니다.
③ 동의를 철회하더라도 기존 서비스 이용에는 영향을 주지 않습니다.

제4조 (개인정보의 보유 및 이용기간)
마케팅 목적으로 수집된 개인정보는 동의철회시 또는 서비스 해지시까지 보유 및 이용됩니다.`
        },
        {
            id: 'location',
            title: '위치정보 이용 동의',
            required: false,
            version: 'v1.0',
            date: '2024.03.15',
            content: `제1조 (위치정보 수집 목적)
회사는 다음의 목적으로 위치정보를 수집·이용합니다:
• 근처 지점 및 ATM 안내
• 위치 기반 맞춤 서비스 제공
• 부정거래 방지 및 보안강화
• 통계작성 및 학술연구

제2조 (위치정보의 수집방법)
위치정보는 다음의 방법으로 수집됩니다:
• GPS를 이용한 정확한 위치정보
• 기지국 정보를 이용한 대략적 위치정보
• Wi-Fi 접속정보를 이용한 위치정보

제3조 (위치정보의 보유 및 이용기간)
① 수집된 위치정보는 수집·이용목적을 달성하는 즉시 파기합니다.
② 다만, 법령에 의해 보관이 의무화된 경우 해당 기간동안 보관합니다.

제4조 (동의철회 및 서비스 중단)
① 위치정보 제공에 대한 동의는 언제든지 철회할 수 있습니다.
② 동의철회시 위치기반 서비스는 제공되지 않으나, 기본적인 금융서비스 이용에는 제한이 없습니다.`
        }
    ];

    const toggleSection = (sectionId) => {
        setExpandedSections(prev => ({
            ...prev,
            [sectionId]: !prev[sectionId]
        }));
    };

    const handleAgreementChange = (termId, checked) => {
        setAgreementStatus(prev => ({
            ...prev,
            [termId]: checked
        }));
    };

    const handleAllMandatoryCheck = (checked) => {
        const mandatoryTerms = termsData.filter(term => term.required);
        const updates = {};

        mandatoryTerms.forEach(term => {
            updates[term.id] = checked;
        });

        setAgreementStatus(prev => ({
            ...prev,
            ...updates
        }));
    };

    useEffect(() => {
        const mandatoryTerms = termsData.filter(term => term.required);
        const allMandatoryChecked = mandatoryTerms.every(term => agreementStatus[term.id]);
        setAllMandatoryAgreed(allMandatoryChecked);
    }, [agreementStatus]);

    const handleComplete = () => {
        if (allMandatoryAgreed) {
            const agreementData = {
                agreements: agreementStatus,
                timestamp: new Date().toISOString(),
                userAgent: navigator.userAgent
            };

            // 안드로이드로 동의 결과 전송
            if (window.AndroidInterface?.completeAgreement) {
                window.AndroidInterface.completeAgreement(JSON.stringify(agreementData));
            } else {
                alert('약관 동의가 완료되었습니다!');
            }
        }
    };

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
                <h1 style={{ margin: '0 0 10px 0', fontSize: '24px' }}>서비스 이용약관</h1>

            </div>

            {/* 전체 필수약관 동의 */}
            <div style={{
                backgroundColor: 'white',
                padding: '20px',
                borderRadius: '12px',
                boxShadow: '0 2px 10px rgba(0,0,0,0.1)',
                marginBottom: '20px',
                border: '2px solid #e74c3c'
            }}>
                <label style={{
                    display: 'flex',
                    alignItems: 'center',
                    cursor: 'pointer',
                    fontSize: '18px',
                    fontWeight: 'bold'
                }}>
                    <input
                        type="checkbox"
                        checked={allMandatoryAgreed}
                        onChange={(e) => handleAllMandatoryCheck(e.target.checked)}
                        style={{
                            width: '20px',
                            height: '20px',
                            marginRight: '12px',
                            cursor: 'pointer'
                        }}
                    />
                    <span style={{ color: '#e74c3c' }}>필수약관 전체동의</span>
                    <span style={{
                        marginLeft: '10px',
                        fontSize: '14px',
                        color: '#7f8c8d',
                        fontWeight: 'normal'
                    }}>
                        (서비스 이용을 위해 필요한 약관입니다)
                    </span>
                </label>
            </div>

            {/* 약관 목록 */}
            {termsData.map((term) => (
                <div key={term.id} style={{
                    backgroundColor: 'white',
                    borderRadius: '12px',
                    boxShadow: '0 2px 10px rgba(0,0,0,0.1)',
                    marginBottom: '15px',
                    overflow: 'hidden'
                }}>
                    {/* 약관 헤더 */}
                    <div style={{
                        padding: '20px',
                        borderBottom: '1px solid #ecf0f1'
                    }}>
                        <div style={{
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'space-between',
                            flexWrap: 'wrap',
                            gap: '10px'
                        }}>
                            <label style={{
                                display: 'flex',
                                alignItems: 'center',
                                cursor: 'pointer',
                                flex: '1',
                                minWidth: '200px'
                            }}>
                                <input
                                    type="checkbox"
                                    checked={agreementStatus[term.id]}
                                    onChange={(e) => handleAgreementChange(term.id, e.target.checked)}
                                    style={{
                                        width: '18px',
                                        height: '18px',
                                        marginRight: '12px',
                                        cursor: 'pointer'
                                    }}
                                />
                                <span style={{
                                    fontSize: '16px',
                                    fontWeight: 'bold',
                                    color: term.required ? '#e74c3c' : '#3498db'
                                }}>
                                    {term.title}
                                </span>
                                <span style={{
                                    marginLeft: '8px',
                                    padding: '2px 8px',
                                    backgroundColor: term.required ? '#e74c3c' : '#95a5a6',
                                    color: 'white',
                                    fontSize: '12px',
                                    borderRadius: '12px',
                                    fontWeight: 'bold'
                                }}>
                                    {term.required ? '필수' : '선택'}
                                </span>
                            </label>

                            <div style={{
                                display: 'flex',
                                alignItems: 'center',
                                gap: '10px'
                            }}>
                                <span style={{
                                    fontSize: '12px',
                                    color: '#7f8c8d',
                                    whiteSpace: 'nowrap'
                                }}>
                                    {term.version} | {term.date}
                                </span>
                                <button
                                    onClick={() => toggleSection(term.id)}
                                    style={{
                                        backgroundColor: '#34495e',
                                        color: 'white',
                                        border: 'none',
                                        padding: '6px 12px',
                                        borderRadius: '6px',
                                        cursor: 'pointer',
                                        fontSize: '12px',
                                        whiteSpace: 'nowrap'
                                    }}
                                >
                                    {expandedSections[term.id] ? '접기' : '전문보기'}
                                </button>
                            </div>
                        </div>
                    </div>

                    {/* 약관 내용 */}
                    {expandedSections[term.id] && (
                        <div style={{
                            padding: '20px',
                            backgroundColor: '#f8f9fa',
                            borderTop: '1px solid #ecf0f1'
                        }}>
                            <div style={{
                                backgroundColor: 'white',
                                padding: '20px',
                                borderRadius: '8px',
                                border: '1px solid #dee2e6',
                                maxHeight: '400px',
                                overflowY: 'auto'
                            }}>
                                <pre style={{
                                    fontFamily: 'inherit',
                                    fontSize: '14px',
                                    lineHeight: '1.6',
                                    color: '#2c3e50',
                                    whiteSpace: 'pre-wrap',
                                    margin: '0'
                                }}>
                                    {term.content}
                                </pre>
                            </div>
                        </div>
                    )}
                </div>
            ))}

            {/* 하단 버튼 영역 */}
            <div style={{
                position: 'sticky',
                bottom: '20px',
                backgroundColor: 'white',
                padding: '20px',
                borderRadius: '12px',
                boxShadow: '0 -2px 10px rgba(0,0,0,0.1)',
                marginTop: '30px'
            }}>
                <div style={{
                    display: 'flex',
                    gap: '15px',
                    justifyContent: 'center',
                    flexWrap: 'wrap'
                }}>
                    <button
                        onClick={() => window.AndroidInterface?.goBack?.()}
                        style={{
                            backgroundColor: '#95a5a6',
                            color: 'white',
                            border: 'none',
                            padding: '15px 25px',
                            borderRadius: '8px',
                            fontSize: '16px',
                            fontWeight: 'bold',
                            cursor: 'pointer',
                            boxShadow: '0 2px 5px rgba(0,0,0,0.2)',
                            minWidth: '120px'
                        }}
                    >
                        이전으로
                    </button>

                    <button
                        onClick={handleComplete}
                        disabled={!allMandatoryAgreed}
                        style={{
                            backgroundColor: allMandatoryAgreed ? '#27ae60' : '#bdc3c7',
                            color: 'white',
                            border: 'none',
                            padding: '15px 25px',
                            borderRadius: '8px',
                            fontSize: '16px',
                            fontWeight: 'bold',
                            cursor: allMandatoryAgreed ? 'pointer' : 'not-allowed',
                            boxShadow: allMandatoryAgreed ? '0 2px 5px rgba(0,0,0,0.2)' : 'none',
                            minWidth: '120px',
                            transition: 'all 0.3s ease'
                        }}
                    >
                        {allMandatoryAgreed ? '✅ 동의완료' : '필수약관 동의 필요'}
                    </button>
                </div>


            </div>
        </div>
    );
};

export default TermsAgreementPage;