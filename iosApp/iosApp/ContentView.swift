import SwiftUI
import UIKit
import WebKit

private let meduzaAppURL: URL = {
    let value = Bundle.main.object(forInfoDictionaryKey: "MeduzaWebAppURL") as? String
    if let value, let url = URL(string: value), url.scheme == "https", url.host != nil { return url }
    return URL(string: "https://meduza.nexa.uz/app/")!
}()

final class MeduzaWebState: ObservableObject {
    @Published var loading = true
    @Published var error: String?
    @Published var reloadID = UUID()
}

struct MeduzaWebView: UIViewRepresentable {
    @ObservedObject var state: MeduzaWebState

    func makeCoordinator() -> Coordinator { Coordinator(state: state) }

    func makeUIView(context: Context) -> WKWebView {
        let config = WKWebViewConfiguration()
        config.websiteDataStore = .default()
        config.allowsInlineMediaPlayback = true
        config.allowsAirPlayForMediaPlayback = true
        config.userContentController.addUserScript(WKUserScript(
            source: "window.__MEDUZA_STANDALONE__ = true;",
            injectionTime: .atDocumentStart, forMainFrameOnly: true))
        let view = WKWebView(frame: .zero, configuration: config)
        view.navigationDelegate = context.coordinator
        view.uiDelegate = context.coordinator
        view.allowsBackForwardNavigationGestures = true
        view.isOpaque = false
        view.backgroundColor = UIColor(red: 0.043, green: 0.051, blue: 0.075, alpha: 1)
        view.scrollView.backgroundColor = view.backgroundColor
        view.load(URLRequest(url: meduzaAppURL))
        return view
    }

    func updateUIView(_ view: WKWebView, context: Context) {
        if context.coordinator.reloadID != state.reloadID {
            context.coordinator.reloadID = state.reloadID
            state.loading = true
            state.error = nil
            view.load(URLRequest(url: meduzaAppURL))
        }
    }

    final class Coordinator: NSObject, WKNavigationDelegate, WKUIDelegate {
        let state: MeduzaWebState
        var reloadID: UUID
        init(state: MeduzaWebState) { self.state = state; self.reloadID = state.reloadID }

        func webView(_ webView: WKWebView, didFinish navigation: WKNavigation!) {
            state.loading = false
            state.error = nil
        }
        func webView(_ webView: WKWebView, didFailProvisionalNavigation navigation: WKNavigation!, withError error: Error) {
            guard (error as NSError).code != NSURLErrorCancelled else { return }
            state.loading = false
            state.error = "Ilovani yuklab bo‘lmadi. Internet aloqasini tekshiring."
        }
        func webView(_ webView: WKWebView, didFail navigation: WKNavigation!, withError error: Error) {
            self.webView(webView, didFailProvisionalNavigation: navigation, withError: error)
        }
        func webView(_ webView: WKWebView, decidePolicyFor action: WKNavigationAction,
                     decisionHandler: @escaping (WKNavigationActionPolicy) -> Void) {
            guard let url = action.request.url else { decisionHandler(.cancel); return }
            if action.targetFrame?.isMainFrame == false {
                decisionHandler(url.scheme == "https" ? .allow : .cancel)
                return
            }
            if url.scheme == "https", url.host == meduzaAppURL.host {
                decisionHandler(.allow)
            } else {
                if url.scheme == "https" || url.scheme == "mailto" { UIApplication.shared.open(url) }
                decisionHandler(.cancel)
            }
        }
        func webView(_ webView: WKWebView, createWebViewWith configuration: WKWebViewConfiguration,
                     for action: WKNavigationAction, windowFeatures: WKWindowFeatures) -> WKWebView? {
            if let url = action.request.url, url.scheme == "https" { UIApplication.shared.open(url) }
            return nil
        }
        private func present(_ alert: UIAlertController, in view: WKWebView) -> Bool {
            var controller = view.window?.rootViewController
            while let next = controller?.presentedViewController { controller = next }
            guard let controller else { return false }
            controller.present(alert, animated: true)
            return true
        }
        func webView(_ view: WKWebView, runJavaScriptAlertPanelWithMessage message: String,
                     initiatedByFrame frame: WKFrameInfo, completionHandler: @escaping () -> Void) {
            let alert = UIAlertController(title: "Meduza", message: message, preferredStyle: .alert)
            alert.addAction(UIAlertAction(title: "OK", style: .default) { _ in completionHandler() })
            if !present(alert, in: view) { completionHandler() }
        }
        func webView(_ view: WKWebView, runJavaScriptConfirmPanelWithMessage message: String,
                     initiatedByFrame frame: WKFrameInfo, completionHandler: @escaping (Bool) -> Void) {
            let alert = UIAlertController(title: "Meduza", message: message, preferredStyle: .alert)
            alert.addAction(UIAlertAction(title: "Bekor qilish", style: .cancel) { _ in completionHandler(false) })
            alert.addAction(UIAlertAction(title: "Davom etish", style: .default) { _ in completionHandler(true) })
            if !present(alert, in: view) { completionHandler(false) }
        }
    }
}

struct ContentView: View {
    @StateObject private var state = MeduzaWebState()
    var body: some View {
        ZStack {
            Color(red: 0.043, green: 0.051, blue: 0.075).ignoresSafeArea()
            MeduzaWebView(state: state)
            if state.loading { ProgressView("Yuklanmoqda…").tint(.cyan).padding().background(.ultraThinMaterial).cornerRadius(12) }
            if let error = state.error {
                VStack(spacing: 16) {
                    Text(error).multilineTextAlignment(.center)
                    Button("Qayta urinish") { state.reloadID = UUID() }.buttonStyle(.borderedProminent)
                }.padding(24).background(.ultraThinMaterial).cornerRadius(16).padding()
            }
        }.preferredColorScheme(.dark)
    }
}
