"use client";

import { useEffect, useRef, useState } from "react";
import { useRouter } from "next/navigation";
import { currentUser, logout, type UserResponse } from "@/lib/api";
import "./home.css";

type Post = {
  meta: string;
  title: string;
  excerpt: string;
  thumb: string;
  body: string[];
};

const SLIDES = [
  { bg: "linear-gradient(135deg,#03122E,#0a2350)", h: "Advisory in action", p: "Sitting alongside founders to turn raw numbers into a clear plan." },
  { bg: "linear-gradient(135deg,#0a2350,#A07C2C)", h: "Capital, unlocked", p: "Preparing women-led SMEs to meet investors with confidence." },
  { bg: "linear-gradient(135deg,#A07C2C,#C9A65A)", h: "The Academy", p: "Workshops that build lasting financial capability across teams." },
  { bg: "linear-gradient(135deg,#223247,#03122E)", h: "Across Africa", p: "Bridging expertise and ambition, one business at a time." },
];

const POSTS: Post[] = [
  {
    meta: "Funding · 5 min read",
    title: "What investors really look for in African SMEs",
    excerpt: "Beyond the pitch deck — the financial signals that move a “maybe” to a “yes”.",
    thumb: "linear-gradient(135deg,#03122E,#0a2350)",
    body: [
      "Investors rarely decide on vision alone. Behind every funded business is a set of numbers that tells a credible, consistent story.",
      "Clean books, a defensible model, and a clear view of unit economics do more to build confidence than any single slide ever could.",
    ],
  },
  {
    meta: "Strategy · 4 min read",
    title: "From messy books to a live financial picture",
    excerpt: "How disciplined bookkeeping quietly becomes a decision-making engine.",
    thumb: "linear-gradient(135deg,#0a2350,#A07C2C)",
    body: [
      "Most founders treat bookkeeping as compliance. The ones who grow fastest treat it as instrumentation.",
      "When your numbers are current and trusted, every decision — pricing, hiring, spending — gets sharper.",
    ],
  },
  {
    meta: "Growth · 6 min read",
    title: "Building an investment-ready data room",
    excerpt: "The documents and models that earn investor confidence fast.",
    thumb: "linear-gradient(135deg,#A07C2C,#C9A65A)",
    body: [
      "A data room is where momentum is won or lost. Disorganisation reads as risk.",
      "Prepare it before you need it: financials, model, cap table, and the narrative that ties them together.",
    ],
  },
];

export default function Home() {
  const router = useRouter();
  const [scrolled, setScrolled] = useState(false);
  const [menuOpen, setMenuOpen] = useState(false);
  const [slide, setSlide] = useState(0);
  const [openPost, setOpenPost] = useState<Post | null>(null);
  const [user, setUser] = useState<UserResponse | null>(null);
  const rootRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    setUser(currentUser());
  }, []);

  const dashboardHref = user?.role === "ADMIN" ? "/admin" : "/portal";

  const signOut = () => {
    logout();
    setUser(null);
    router.refresh();
  };

  useEffect(() => {
    const onScroll = () => setScrolled(window.scrollY > 40);
    onScroll();
    window.addEventListener("scroll", onScroll);
    return () => window.removeEventListener("scroll", onScroll);
  }, []);

  useEffect(() => {
    document.documentElement.classList.add("js");
    const root = rootRef.current;
    if (!root) return;

    const revObserver = new IntersectionObserver(
      (entries) => {
        entries.forEach((e) => {
          if (e.isIntersecting) {
            e.target.classList.add("in");
            revObserver.unobserve(e.target);
          }
        });
      },
      { threshold: 0.15 }
    );
    root.querySelectorAll<HTMLElement>(".reveal").forEach((el) => revObserver.observe(el));

    const numObserver = new IntersectionObserver(
      (entries) => {
        entries.forEach((e) => {
          if (e.isIntersecting) {
            animateCount(e.target as HTMLElement);
            numObserver.unobserve(e.target);
          }
        });
      },
      { threshold: 0.6 }
    );
    root.querySelectorAll<HTMLElement>(".num[data-target]").forEach((el) => numObserver.observe(el));

    return () => {
      revObserver.disconnect();
      numObserver.disconnect();
    };
  }, []);

  useEffect(() => {
    document.body.style.overflow = openPost ? "hidden" : "";
    return () => {
      document.body.style.overflow = "";
    };
  }, [openPost]);

  const closeMenu = () => setMenuOpen(false);

  const sendMessage = () => {
    const val = (id: string) =>
      (document.getElementById(id) as HTMLInputElement | HTMLTextAreaElement | null)?.value.trim() || "";
    const name = val("fName");
    const email = val("fEmail");
    const company = val("fCompany");
    const msg = val("fMsg");
    if (!name || !msg) {
      alert("Please add your name and a short message.");
      return;
    }
    const subject = encodeURIComponent("UFB enquiry from " + name + (company ? " (" + company + ")" : ""));
    const bodyText = encodeURIComponent(
      "Name: " + name + "\n" + "Email: " + email + "\n" + "Company: " + company + "\n\n" + msg
    );
    window.location.href = "mailto:info@ufbconsulting.com?subject=" + subject + "&body=" + bodyText;
  };

  return (
    <div ref={rootRef}>
      <header className={scrolled ? "scrolled" : ""}>
        <div className="wrap nav">
          <a className="brand" href="#top" aria-label="UFB Consulting home">
            <img className="mark" src="/ufb-logo.png" alt="UFB Consulting — Unified Finance Bridge" />
          </a>
          <nav>
            <a href="#about">About</a>
            <a href="#services">Services</a>
            <a href="#work">Work</a>
            <a href="#insights">Insights</a>
            <a href="#contact">Contact</a>
          </nav>
          <div className="auth-nav">
            {user ? (
              <>
                <span className="auth-hello">Hi, {user.fullName.split(" ")[0]}</span>
                <a href={dashboardHref} className="cta">Dashboard</a>
                <button type="button" className="auth-link" onClick={signOut}>Sign out</button>
              </>
            ) : (
              <>
                <a href="/login" className="auth-link">Sign in</a>
                <a href="/register" className="cta">Sign Up</a>
              </>
            )}
          </div>
          <div
            className={"burger" + (menuOpen ? " open" : "")}
            aria-label="Menu"
            role="button"
            tabIndex={0}
            onClick={() => setMenuOpen((o) => !o)}
          >
            <span></span><span></span><span></span>
          </div>
        </div>
      </header>

      <div className={"mobile-menu" + (menuOpen ? " open" : "")}>
        <a href="#about" onClick={closeMenu}>About</a>
        <a href="#services" onClick={closeMenu}>Services</a>
        <a href="#work" onClick={closeMenu}>Work</a>
        <a href="#insights" onClick={closeMenu}>Insights</a>
        <a href="#contact" onClick={closeMenu}>Contact</a>
        <a href="#contact" className="cta" onClick={closeMenu}>Work With Us</a>
        {user ? (
          <>
            <a href={dashboardHref} onClick={closeMenu}>Dashboard</a>
            <a href="#top" onClick={() => { signOut(); closeMenu(); }}>Sign out</a>
          </>
        ) : (
          <>
            <a href="/login" onClick={closeMenu}>Sign in</a>
            <a href="/register" className="cta" onClick={closeMenu}>Sign Up</a>
          </>
        )}
      </div>

      <section className="hero" id="top">
        <div className="ring r1"></div>
        <div className="ring r2"></div>
        <svg className="bridge" viewBox="0 0 1200 160" preserveAspectRatio="none" aria-hidden="true">
          <path d="M0 150 Q600 0 1200 150" fill="none" stroke="#C9A65A" strokeWidth="1.5" />
          <line x1="0" y1="150" x2="1200" y2="150" stroke="#C9A65A" strokeWidth="1.5" />
          <g stroke="#C9A65A" strokeWidth="1">
            <line x1="150" y1="113" x2="150" y2="150" />
            <line x1="350" y1="78" x2="350" y2="150" />
            <line x1="600" y1="65" x2="600" y2="150" />
            <line x1="850" y1="78" x2="850" y2="150" />
            <line x1="1050" y1="113" x2="1050" y2="150" />
          </g>
        </svg>
        <div className="wrap hero-in">
          <span className="kicker">Unified Finance Bridge · Consulting</span>
          <h1>Bridging finance to <em>growth</em> for Africa&rsquo;s ambitious businesses.</h1>
          <p className="lead">A tech-native financial advisory firm helping growth-stage businesses turn numbers into clarity, foresight, and access to capital.</p>
          <div className="btns">
            <a href="#contact" className="btn-g">Start a Conversation</a>
            <a href="#services" className="btn-o">Explore Services</a>
          </div>
        </div>
        <div className="tagstrip">
          <div className="wrap">
            <span><b>Advisory</b></span>
            <span><b>Capital</b></span>
            <span><b>Academy</b></span>
            <span className="cin" style={{ color: "#c7d0de", fontSize: "14px", letterSpacing: "1px", fontStyle: "italic" }}>Where Capital Meets Expertise</span>
          </div>
        </div>
      </section>

      <section className="stats" id="stats">
        <div className="wrap">
          <div className="stats-grid">
            <div className="stat-item reveal"><div className="num" data-target="50" data-suffix="+">0</div><div className="lbl">Businesses supported</div></div>
            <div className="stat-item reveal"><div className="num" data-prefix="$" data-target="12" data-suffix="M+">0</div><div className="lbl">Capital facilitated</div></div>
            <div className="stat-item reveal"><div className="num" data-target="3" data-suffix="">0</div><div className="lbl">Service pillars</div></div>
            <div className="stat-item reveal"><div className="num" data-target="100" data-suffix="%">0</div><div className="lbl">Founder-focused</div></div>
          </div>
        </div>
      </section>

      <section className="about" id="about">
        <div className="wrap">
          <div className="reveal">
            <div className="eyebrow">Who We Are</div>
            <h2 className="sec-title">A finance partner built<br />for the way you grow.</h2>
            <p className="sec-sub">UFB Consulting helps growth-stage African businesses make confident financial decisions — combining hands-on advisory with modern technology to make finance make sense.</p>
          </div>
          <div className="about-grid">
            <div className="reveal">
              <div className="stat"><div className="n">01</div><div className="t"><strong>Clarity.</strong> We turn messy books into a clear, live picture of your business&rsquo;s health.</div></div>
              <div className="stat"><div className="n">02</div><div className="t"><strong>Foresight.</strong> We model what&rsquo;s ahead, so you plan with numbers instead of instinct.</div></div>
              <div className="stat"><div className="n">03</div><div className="t"><strong>Capital.</strong> We make your business investment-ready and help you access funding.</div></div>
            </div>
            <div className="panel reveal">
              <h3>Where Capital Meets Expertise</h3>
              <p>We believe every ambitious business deserves the kind of financial guidance usually reserved for the largest companies — delivered with judgement, technology, and a genuine understanding of the local market.</p>
            </div>
          </div>
        </div>
      </section>

      <section className="services" id="services">
        <div className="wrap">
          <div className="reveal center" style={{ maxWidth: "640px" }}>
            <div className="eyebrow">What We Do</div>
            <h2 className="sec-title">Three bridges to growth.</h2>
            <p className="sec-sub center">One firm, three connected service lines — meeting your business wherever it is on its journey.</p>
          </div>
          <div className="cards">
            <div className="card reveal">
              <div className="num">BRIDGE 01</div>
              <h3>UFB Advisory</h3>
              <p>Financial clarity and guidance, powered by technology.</p>
              <ul><li>Bookkeeping &amp; financial management</li><li>Performance dashboards &amp; analysis</li><li>Strategy &amp; decision support</li></ul>
            </div>
            <div className="card reveal">
              <div className="num">BRIDGE 02</div>
              <h3>UFB Capital</h3>
              <p>From investment-ready to funded.</p>
              <ul><li>Investment readiness &amp; data rooms</li><li>Financial models &amp; valuations</li><li>Access to investors &amp; lenders</li></ul>
            </div>
            <div className="card reveal">
              <div className="num">BRIDGE 03</div>
              <h3>UFB Academy</h3>
              <p>Building financial capability that lasts.</p>
              <ul><li>Founder &amp; team training</li><li>Financial literacy programs</li><li>Workshops &amp; resources</li></ul>
            </div>
          </div>
        </div>
      </section>

      <section className="gallery" id="work">
        <div className="wrap">
          <div className="reveal center" style={{ maxWidth: "640px" }}>
            <div className="eyebrow">In The Field</div>
            <h2 className="sec-title">Finance, made human.</h2>
            <p className="sec-sub center">A look at the work, the partnerships, and the businesses we help bring to life.</p>
          </div>
          <div className="carousel reveal">
            <div className="track" style={{ transform: `translateX(-${slide * 100}%)` }}>
              {SLIDES.map((s, i) => (
                <div className="slide" key={i} style={{ background: s.bg }}>
                  <div className="scrim"></div>
                  <div className="caption"><h3>{s.h}</h3><p>{s.p}</p></div>
                </div>
              ))}
            </div>
            <button className="car-btn car-prev" aria-label="Previous" onClick={() => setSlide((s) => (s - 1 + SLIDES.length) % SLIDES.length)}>&#10094;</button>
            <button className="car-btn car-next" aria-label="Next" onClick={() => setSlide((s) => (s + 1) % SLIDES.length)}>&#10095;</button>
          </div>
          <div className="dots">
            {SLIDES.map((_, i) => (
              <button key={i} className={"dot" + (i === slide ? " active" : "")} aria-label={`Go to slide ${i + 1}`} onClick={() => setSlide(i)} />
            ))}
          </div>
        </div>
      </section>

      <section className="approach" id="approach">
        <div className="wrap">
          <div className="reveal center" style={{ maxWidth: "640px" }}>
            <div className="eyebrow" style={{ color: "var(--gold)" }}>How We Work</div>
            <h2 className="sec-title">A clear path, every time.</h2>
            <p className="sec-sub center">We make the process simple — so you always know where you stand.</p>
          </div>
          <div className="steps">
            <div className="step reveal"><div className="sn">01</div><h4>Understand</h4><p>We listen and learn your business, goals, and numbers.</p></div>
            <div className="step reveal"><div className="sn">02</div><h4>Diagnose</h4><p>We turn your finances into clear insight and honest findings.</p></div>
            <div className="step reveal"><div className="sn">03</div><h4>Plan</h4><p>We build the model, the strategy, and the path forward.</p></div>
            <div className="step reveal"><div className="sn">04</div><h4>Grow</h4><p>We support you as you execute, fund, and scale.</p></div>
          </div>
        </div>
      </section>

      <section className="insights" id="insights">
        <div className="wrap">
          <div className="ins-head reveal">
            <div style={{ maxWidth: "640px" }}>
              <div className="eyebrow">Insights</div>
              <h2 className="sec-title">From the bridge.</h2>
              <p className="sec-sub">Ideas on finance, funding, and building stronger businesses across Africa.</p>
            </div>
          </div>
          <div className="posts">
            {POSTS.map((post, i) => (
              <div className="post" key={i} onClick={() => setOpenPost(post)}>
                <div className="thumb" style={{ background: post.thumb }}></div>
                <div className="body">
                  <div className="meta">{post.meta}</div>
                  <h3>{post.title}</h3>
                  <p>{post.excerpt}</p>
                  <div className="more">Read more →</div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      <div
        className={"modal" + (openPost ? " open" : "")}
        onClick={(e) => { if (e.target === e.currentTarget) setOpenPost(null); }}
      >
        {openPost && (
          <div className="modal-card">
            <button className="modal-close" aria-label="Close" onClick={() => setOpenPost(null)}>&times;</button>
            <div className="hero-img" style={{ background: openPost.thumb }}></div>
            <div className="inner">
              <div className="meta">{openPost.meta}</div>
              <h2>{openPost.title}</h2>
              <div className="article">
                {openPost.body.map((para, i) => (<p key={i}>{para}</p>))}
              </div>
            </div>
          </div>
        )}
      </div>

      <section className="contact" id="contact">
        <div className="wrap">
          <div className="reveal center" style={{ maxWidth: "640px" }}>
            <div className="eyebrow">Get In Touch</div>
            <h2 className="sec-title">Let&rsquo;s bridge your finance to growth.</h2>
            <p className="sec-sub center">Whether you need clarity on your numbers, a model for investors, or a partner for the journey — we&rsquo;d love to talk.</p>
          </div>
          <div className="contact-grid">
            <div className="contact-info reveal">
              <a className="ci" href="mailto:info@ufbconsulting.com">
                <span className="ico">&#9993;</span><span><span className="lbl">Email</span><span className="val">info@ufbconsulting.com</span></span>
              </a>
              <a className="ci" href="tel:+250781141576">
                <span className="ico">&#9742;</span><span><span className="lbl">Phone</span><span className="val">+250 781 141 576</span></span>
              </a>
              <div className="ci">
                <span className="ico">&#9873;</span><span><span className="lbl">Location</span><span className="val">Kigali, Rwanda</span></span>
              </div>
              <a className="ci" href="https://www.ufbconsulting.com">
                <span className="ico">&#127760;</span><span><span className="lbl">Web</span><span className="val">www.ufbconsulting.com</span></span>
              </a>
            </div>
            <div className="form reveal">
              <div className="row"><label>Name</label><input id="fName" placeholder="Your name" /></div>
              <div className="row"><label>Email</label><input id="fEmail" type="email" placeholder="you@company.com" /></div>
              <div className="row"><label>Company</label><input id="fCompany" placeholder="Your business" /></div>
              <div className="row"><label>How can we help?</label><textarea id="fMsg" placeholder="Tell us a little about your business and what you need."></textarea></div>
              <button className="submit" onClick={sendMessage}>Send Message</button>
              <p className="note">This opens your email app, pre-addressed to UFB.</p>
            </div>
          </div>
        </div>
      </section>

      <footer>
        <div className="wrap">
          <div className="fgrid">
            <div>
              <a className="brand" href="#top">
                <img className="mark" src="/ufb-logo.png" alt="UFB Consulting — Unified Finance Bridge" style={{ height: "62px", width: "auto" }} />
              </a>
              <p className="ftag">Where Capital Meets Expertise</p>
              <p style={{ maxWidth: "320px", fontSize: "14px" }}>A tech-native financial advisory firm bridging finance to growth for Africa&rsquo;s ambitious businesses.</p>
            </div>
            <div className="fcol">
              <h5>Explore</h5>
              <a href="#about">About</a><a href="#services">Services</a><a href="#work">Work</a><a href="#insights">Insights</a><a href="#contact">Contact</a>
            </div>
            <div className="fcol">
              <h5>Contact</h5>
              <p>info@ufbconsulting.com</p>
              <p>www.ufbconsulting.com</p>
              <p>+250 781 141 576</p>
              <p>Kigali, Rwanda</p>
            </div>
          </div>
          <div className="fbottom">
            <span>&copy; {new Date().getFullYear()} Unified Finance Bridge Consulting. All rights reserved.</span>
            <span>Advisory &middot; Capital &middot; Academy</span>
          </div>
        </div>
      </footer>
    </div>
  );
}

function animateCount(el: HTMLElement) {
  const target = parseFloat(el.dataset.target || "0");
  const prefix = el.dataset.prefix || "";
  const suffix = el.dataset.suffix || "";
  const dur = 1600;
  const start = performance.now();
  function tick(now: number) {
    const t = Math.min((now - start) / dur, 1);
    const eased = 1 - Math.pow(1 - t, 3);
    el.textContent = prefix + Math.round(target * eased) + suffix;
    if (t < 1) requestAnimationFrame(tick);
  }
  requestAnimationFrame(tick);
}