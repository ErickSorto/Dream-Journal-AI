import Foundation
import UIKit

@objc class PdfGenerator: NSObject {
    @objc func createPdfData(from text: String) -> NSData? {
        let pdfRenderer = UIGraphicsPDFRenderer(bounds: CGRect(x: 0, y: 0, width: 8.5 * 72.0, height: 11.0 * 72.0))
        
        let nsString = text as NSString
        
        let attributes: [NSAttributedString.Key: Any] = [
            .font: UIFont.systemFont(ofSize: 12)
        ]
        
        let data = pdfRenderer.pdfData { (context) in
            context.beginPage()
            nsString.draw(in: CGRect(x: 72.0, y: 72.0, width: 8.5 * 72.0 - 144.0, height: 11.0 * 72.0 - 144.0), withAttributes: attributes)
        }
        
        return data as NSData
    }
}
